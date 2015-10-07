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
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.testng.Assert.assertEquals;

/**
 * @author mamo
 */
@Test(sequential = true)
public class SimpleIdGeneratorTest extends IdGeneratorBaseTest {

    @Test
    public void afterInit() throws SQLException {
        assertEquals(getCurrentInTableId(), (Long) IdGenerator.NO_ID, "Wrong init db value");
        assertEquals(getCurrentInMemoryId(), IdGenerator.NO_ID, "Wrong init in memory");
    }

    @Test(dependsOnMethods = "afterInit")
    public void firstId() throws SQLException {
        assertEquals(idGenerator.nextId(), 1, "First nextId is lazy initialized to 1");
        assertEquals(getCurrentInTableId(), step(), "First nextId should not update db");
        assertEquals(getCurrentInMemoryId(), 2, "First nextId should update in memory by 1");
    }

    @Test(dependsOnMethods = "firstId")
    public void nextId() throws SQLException {
        assertEquals(idGenerator.nextId(), 2);
        assertEquals(getCurrentInTableId(), step(), "Second nextId should not update db");
        assertEquals(getCurrentInMemoryId(), 3, "Second nextId should increment by 1");
    }

    @Test(dependsOnMethods = "nextId")
    public void beforeExhaust() throws SQLException {
        while (getCurrentInMemoryId() <= step()) {
            long inMemory = getCurrentInMemoryId();
            long nextId = idGenerator.nextId();
            assertEquals(inMemory, nextId, "In memory value is the nextId"); //getAndInc
            assertEquals(getCurrentInMemoryId(), nextId + 1, "In memory should increment by 1");
        }
        assertEquals(getCurrentInTableId(), step(), "Wrong before exhaust db value");
        assertEquals(getCurrentInMemoryId(), step() + 1, "In memory exhausted before db");
    }

    @Test(dependsOnMethods = "beforeExhaust")
    public void afterExhaust() throws SQLException {
        long nextId = idGenerator.nextId();
        assertEquals(nextId, step() + 1, "Wrong exhaust nextId value");
        assertEquals(getCurrentInTableId(), new Long(2 * step()), "Wrong after exhaust db value");
        assertEquals(getCurrentInMemoryId(), step() + 2, "Wrong after exhaust in memory");
    }

    private static Long step() {
        return ConstantValues.dbIdGeneratorFetchAmount.getLong();
    }
}
