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

package org.artifactory.storage.db;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link org.artifactory.storage.db.DbType}.
 *
 * @author Yossi Shaul
 */
@Test
public class DbTypeTest {

    public void parseSupported() {
        assertEquals(DbType.DERBY, DbType.parse("derby"));
    }

    public void parseSupportedCapitals() {
        assertEquals(DbType.ORACLE, DbType.parse("ORACLE"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void parseNull() {
        DbType.parse(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void parseEmpty() {
        DbType.parse("");
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*nosql.*")
    public void parseNotSupported() {
        DbType.parse("nosql");
    }
}
