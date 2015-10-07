/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.storage.db.ds;

import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.spring.ArtifactoryTomcatDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.testng.Assert.*;

/**
 * Tests the connection pooling done with {@link org.artifactory.storage.db.spring.ArtifactoryTomcatDataSource}.
 *
 * @author Yossi Shaul
 */
@Test
public class ArtifactoryTomcatDataSourceTest extends DbBaseTest {

    @Autowired
    private DataSource dataSource;

    public void instanceType() {
        boolean ofExpectedType = dataSource instanceof ArtifactoryTomcatDataSource;
        assertTrue(ofExpectedType, "Unexpected datasource type: " + dataSource.getClass());
    }

    public void verifyDefaultParams() {
        ArtifactoryTomcatDataSource ds = (ArtifactoryTomcatDataSource) dataSource;
        assertEquals(ds.getDefaultTransactionIsolation(), Connection.TRANSACTION_READ_COMMITTED);
    }

    public void verifyValidationQuery() throws SQLException {
        ArtifactoryTomcatDataSource ds = (ArtifactoryTomcatDataSource) dataSource;
        String validationQuery = ds.getValidationQuery();
        assertNotNull(validationQuery, "Validation query shouldn't be null");

        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(validationQuery)) {
            if (!rs.next()) {
                fail("No result returned from the validation query");
            }
        }
    }
}
