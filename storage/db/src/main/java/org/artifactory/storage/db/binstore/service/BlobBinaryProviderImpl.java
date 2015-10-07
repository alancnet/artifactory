/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.storage.db.binstore.service;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.binstore.BinaryInfo;
import org.artifactory.io.checksum.Sha1Md5ChecksumInputStream;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.binstore.service.BinaryInfoImpl;
import org.artifactory.storage.binstore.service.BinaryNotFoundException;
import org.artifactory.storage.binstore.service.BinaryProviderBase;
import org.artifactory.storage.binstore.service.annotation.BinaryProviderClassInfo;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.DbType;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.util.blob.BlobWrapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.artifactory.storage.db.binstore.dao.BinariesDao.TEMP_SHA1_PREFIX;

/**
 * Date: 12/12/12
 * Time: 4:29 PM
 *
 * @author freds
 */
@BinaryProviderClassInfo(nativeName = "blob")
public class BlobBinaryProviderImpl extends BinaryProviderBase {
    private static final Logger log = LoggerFactory.getLogger(BlobBinaryProviderImpl.class);

    private JdbcHelper jdbcHelper;
    private DbType databaseType;
    private BlobWrapperFactory blobsFactory;

    @Override
    public void initialize() {
        ArtifactoryContext context = ContextHelper.get();
        this.databaseType = context.beanForType(DbService.class).getDatabaseType();
        this.blobsFactory = context.beanForType(BlobWrapperFactory.class);
        jdbcHelper = context.beanForType(JdbcHelper.class);
        if (jdbcHelper == null) {
            throw new IllegalArgumentException("Cannot create Blob binary provider without JDBC Helper!");
        }
    }

    @Override
    public boolean exists(String sha1, long length) {
        try {
            long lengthFound;
            if (databaseType != DbType.MSSQL) {
                lengthFound = jdbcHelper.executeSelectLong(
                        "SELECT LENGTH(data) FROM binary_blobs where sha1 = ?", sha1);
            } else {    // mssql use len() function
                lengthFound = jdbcHelper.executeSelectLong("SELECT DATALENGTH(data) FROM binary_blobs where sha1 = ?", sha1);
            }
            if (lengthFound != DbService.NO_DB_ID) {
                log.trace("Found sha1 {} with length {}", sha1, length);
                if (length != lengthFound) {
                    throw new BinaryNotFoundException("Found a file with checksum '"
                            + sha1 + "' but length is " + lengthFound + " not " + length);
                }
                return true;
            }
        } catch (SQLException e) {
            throw new StorageException("Could not verify existence of " + sha1, e);
        }
        return next().exists(sha1, length);
    }

    @Nonnull
    @Override
    public InputStream getStream(String sha1) throws BinaryNotFoundException {
        try {
            /*if (TransactionSynchronizationManager.isSynchronizationActive()) {
                throw new StorageException("Cannot retrieve binary data of " +
                        sha1 + " since the datasource is in transaction!");
            }*/
            ResultSet rs = jdbcHelper.executeSelect("SELECT data FROM binary_blobs where sha1 = ?", sha1);
            if (rs.next()) {
                return new BlobStream(rs.getBinaryStream(1), rs);
            } else {
                DbUtils.close(rs);
            }
        } catch (SQLException e) {
            throw new StorageException("Could not select content for " + sha1, e);
        }
        return next().getStream(sha1);
    }

    @Nonnull
    @Override
    public BinaryInfo addStream(InputStream is) throws IOException {
        Sha1Md5ChecksumInputStream checksumStream = null;
        try {
            // first save to a temp file and calculate checksums while saving
            if (is instanceof Sha1Md5ChecksumInputStream) {
                checksumStream = (Sha1Md5ChecksumInputStream) is;
            } else {
                checksumStream = new Sha1Md5ChecksumInputStream(is);
            }
            // Create a dummy ID
            String randomId = TEMP_SHA1_PREFIX + RandomStringUtils.randomAlphanumeric(40 - TEMP_SHA1_PREFIX.length());
            int inserted = jdbcHelper.executeUpdate("INSERT INTO binary_blobs VALUES (?,?)", randomId,
                    blobsFactory.create(checksumStream));
            if (inserted != 1) {
                throw new StorageException("Stream failed with unknown reason! Total line inserted was " + inserted);
            }
            checksumStream.close();
            BinaryInfo bd = new BinaryInfoImpl(checksumStream);
            log.trace("Inserting {} in blob binary provider", bd);

            String sha1 = bd.getSha1();
            if (!exists(sha1, bd.getLength())) {
                int updated = 0;
                try {
                    updated = jdbcHelper.executeUpdate("UPDATE binary_blobs SET sha1 = ? WHERE sha1 = ?",
                            sha1, randomId);
                } catch (SQLException e) {
                    // May get duplicate key violated on concurrent insert
                    // TORE: [by fsi] with good SQL error handling should limit to duplicate key violation cases
                    log.debug("Got exception moving temp blob line " + randomId + " to " + sha1, e);
                    if (exists(sha1, bd.getLength())) {
                        // All is OK someone else did the job already
                        deleteTempRow(randomId, sha1);
                        updated = 1;
                    }
                }
                if (updated != 1) {
                    deleteTempRow(randomId, sha1);
                    throw new StorageException(
                            "Could not update line " + randomId + " with " + sha1
                                    + " ! Total line updated was " + updated);
                }
            } else {
                deleteTempRow(randomId, sha1);
            }

            return bd;
        } catch (SQLException e) {
            throw new StorageException("Could not insert Stream due to:" + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(checksumStream);
        }
    }

    private void deleteTempRow(String randomId, String sha1) throws SQLException {
        int deleted = jdbcHelper.executeUpdate("DELETE FROM binary_blobs WHERE sha1 = ?", randomId);
        if (deleted != 1) {
            throw new StorageException(
                    "Deletion of temporary line " + randomId + " which match sha1='" + sha1
                            + "' did not return 1!\nTotal line deleted was " + deleted);
        }
    }

    @Override
    public boolean delete(String sha1) {
        try {
            int deleted = jdbcHelper.executeUpdate("DELETE FROM binary_blobs WHERE sha1 = ?", sha1);
            if (deleted != 1) {
                log.warn("Deletion of blob entry {} did not update any lines! Got {}", sha1, deleted);
            }
        } catch (SQLException e) {
            throw new StorageException("Could not insert Stream due to:" + e.getMessage(), e);
        }
        return next().delete(sha1);
    }

    static class BlobStream extends FilterInputStream {
        private final ResultSet resultSet;
        private boolean closed = false;

        BlobStream(InputStream in, ResultSet resultSet) {
            super(in);
            this.resultSet = resultSet;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                if (!closed) {
                    closed = true;
                    DbUtils.close(resultSet);
                }
            }
        }
    }
}
