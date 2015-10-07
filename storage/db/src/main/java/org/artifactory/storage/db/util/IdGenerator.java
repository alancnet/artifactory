/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.storage.db.util;

import org.artifactory.common.ConstantValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Date: 12/3/13 3:03 PM
 *
 * @author freds
 */
public abstract class IdGenerator {
    private static final Logger log = LoggerFactory.getLogger(IdGenerator.class);

    public static final String INDEX_TYPE_GENERAL = "general";
    public static final long NO_ID = 0;

    public static long step() {
        return ConstantValues.dbIdGeneratorFetchAmount.getLong();
    }

    @Autowired
    @Qualifier("uniqueIdsDataSource")
    protected DataSource uniqueIdsDataSource;

    @Autowired
    private JdbcHelper jdbcHelper;

    @Transactional
    public void initializeIdGenerator() throws SQLException {
        if (step() <= 0) {
            throw new IllegalArgumentException("IdGenerator STEP must be positive");
        }

        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT current_id FROM unique_ids WHERE index_type = ?", INDEX_TYPE_GENERAL);
            if (!rs.next()) {
                int rows = jdbcHelper.executeUpdate("INSERT INTO unique_ids VALUES (?, ?)", INDEX_TYPE_GENERAL, NO_ID);
                if (rows == 1) {
                    log.debug("Created current unique id for the first time");
                }
                initializeIndex(NO_ID);
            } else {
                initializeIndex(rs.getLong(1));
            }
        } catch (Exception e) {
            throw new SQLException("Could not select current index.", e);
        } finally {
            DbUtils.close(rs);
        }
    }

    protected abstract void initializeIndex(long currentValue);

    public abstract long nextId();
}
