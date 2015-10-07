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

package org.artifactory.storage.db.upgrades.v310;

import org.artifactory.storage.db.itest.DbTestUtils;
import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.artifactory.storage.db.util.DbUtils;

import java.sql.Connection;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Date: 8/5/13 9:58 AM
 *
 * @author freds
 */
public class UpgradeTo310 extends UpgradeBaseTest {

    public void testDdlUpgrade() throws Exception {
        rollBackTo300Version();
        // Now the DB is like in 3.0.x, should be missing the new tables of 3.1.x
        try (Connection connection = jdbcHelper.getDataSource().getConnection()) {
            assertTrue(DbTestUtils.isTableMissing(connection));
            DbUtils.executeSqlStream(connection, getDbSchemaUpgradeSql("v310", storageProperties.getDbType()));
            assertFalse(DbTestUtils.isTableMissing(connection));
        }
    }

}
