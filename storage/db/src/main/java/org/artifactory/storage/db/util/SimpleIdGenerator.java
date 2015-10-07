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

import org.artifactory.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author mamo
 */
@Service
public class SimpleIdGenerator extends IdGenerator {
    private static final Logger log = LoggerFactory.getLogger(SimpleIdGenerator.class);

    protected final Object indexMonitor = new Object();
    protected final AtomicLong currentIndex = new AtomicLong(NO_ID);
    protected volatile long maxReservedIndex = NO_ID;

    @Override
    protected void initializeIndex(long currentValue) {
        maxReservedIndex = currentValue;
        if (currentValue != NO_ID) {
            currentValue++;
        }
        currentIndex.set(currentValue);
        log.debug("Initialized current index to " + currentIndex.get());
    }

    @Override
    public long nextId() {
        long value = currentIndex.getAndIncrement();
        if (isNotInRange(value)) {
            synchronized (indexMonitor) {
                value = getGoodValue(value);
            }
        }
        return value;
    }

    private long getGoodValue(long value) {
        if (isNotInRange(value)) {
            simpleUpdateIndex();
            if (value == NO_ID) {
                value = currentIndex.getAndIncrement();
            }
        }
        return value;
    }

    private boolean isNotInRange(long value) {
        return value > maxReservedIndex || value == NO_ID;
    }

    private void simpleUpdateIndex() {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = uniqueIdsDataSource.getConnection();
            stmt = con.prepareStatement("UPDATE unique_ids SET current_id = ? where index_type = ?");
            try {
                long nextMaxCurrentIndex = maxReservedIndex + step();
                stmt.setLong(1, nextMaxCurrentIndex);
                stmt.setString(2, INDEX_TYPE_GENERAL);
                int rows = stmt.executeUpdate();
                if (rows == 1) {
                    maxReservedIndex = nextMaxCurrentIndex;
                } else {
                    throw new StorageException(
                            "Failed to update the unique indices table " + INDEX_TYPE_GENERAL + " does not exists!");
                }
            } catch (SQLException e) {
                throw new StorageException("Failed to update the unique indices table", e);
            }
        } catch (SQLException e) {
            throw new StorageException("Failed to update the unique indices table", e);
        } finally {
            DbUtils.close(con, stmt, null, uniqueIdsDataSource);
        }
    }

    @PreDestroy
    private void destroy() {
        DbUtils.closeDataSource(uniqueIdsDataSource);
    }
}
