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

package org.artifactory.storage.binstore.service.providers;

import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.binstore.service.BinaryNotFoundException;
import org.artifactory.storage.binstore.service.BinaryProviderBase;
import org.artifactory.storage.binstore.service.BinaryProviderHelper;
import org.artifactory.storage.binstore.service.FileBinaryProvider;
import org.artifactory.storage.binstore.service.FileProviderStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static org.artifactory.storage.binstore.service.BinaryProviderHelper.getDataFolder;

/**
 * Date: 12/16/12
 * Time: 10:44 AM
 *
 * @author freds
 */
public abstract class FileBinaryProviderReadOnlyBase extends BinaryProviderBase
        implements FileBinaryProvider, FileProviderStrategy {
    private static final Logger log = LoggerFactory.getLogger(FileBinaryProviderReadOnlyBase.class);

    protected File binariesDir;
    protected File tempBinariesDir;

    @Override
    public void initialize() {
        // Main filestore directory
        String binaryProviderDir = getStorageProperties().getBinaryProviderDir();
        if (StringUtils.isBlank(binaryProviderDir)) {
            binaryProviderDir = "filestore";
        }
        binariesDir = getDataFolder(getBaseDataDir(), getParam("dir", binaryProviderDir));
        this.tempBinariesDir = getNewTempBinariesFile(binariesDir);
        verifyState(binariesDir);
    }

    protected File getBaseDataDir() {
        return ArtifactoryHome.get().getHaAwareDataDir();
    }


    protected File getNewTempBinariesFile(File binariesDir) {
        return new File(binariesDir, "_pre");
    }

    @Override
    public boolean isAccessible() {
        try {
            verifyState(binariesDir);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected void verifyState(File binariesDir) {
        if (!binariesDir.exists() && !binariesDir.mkdirs()) {
            throw new StorageException("Could not create file store folder '" + binariesDir.getAbsolutePath() + "'");
        }
        File tempDir = getNewTempBinariesFile(binariesDir);
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new StorageException("Could not create temporary pre store folder '" +
                    tempDir.getAbsolutePath() + "'");
        }
    }

    @Override
    public File getBinariesDir() {
        return binariesDir;
    }

    @Override
    public boolean exists(String sha1, long length) {
        File file = getFile(sha1);
        if (file.exists()) {
            log.trace("File found: {}", file.getAbsolutePath());
            if (file.length() != length) {
                log.error("Found a file with checksum '" + sha1 + "' " +
                        "but length is " + file.length() + " not " + length);
                return false;
            }
            return true;
        } else {
            log.trace("File not found: {}", file.getAbsolutePath());
        }
        return next().exists(sha1, length);
    }

    @Override
    @Nonnull
    public File getFile(String sha1) {
        return new File(binariesDir, BinaryProviderHelper.getRelativePath(sha1));
    }

    @Override
    public InputStream getStream(String sha1) {
        File file = getFile(sha1);
        try {
            if (!file.exists()) {
                log.trace("File not found: {}", file.getAbsolutePath());
                return next().getStream(sha1);
            }
            log.trace("File found: {}", file.getAbsolutePath());
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new BinaryNotFoundException("Couldn't access file '" + file.getAbsolutePath() + "'", e);
        }
    }

    @Nonnull
    @Override
    public File getTempBinariesDir(Random random) throws IOException {
        return BinaryProviderHelper.createTempBinFile(tempBinariesDir);
    }

}
