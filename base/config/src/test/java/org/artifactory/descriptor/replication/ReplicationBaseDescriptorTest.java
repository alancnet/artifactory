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

package org.artifactory.descriptor.replication;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Noam Y. Tenne
 */
@Test
public abstract class ReplicationBaseDescriptorTest<T extends ReplicationBaseDescriptor> {

    @Test
    public void testDefaultBaseValues() throws Exception {
        T replicationDescriptor = constructDescriptor();
        assertFalse(replicationDescriptor.isEnabled(), "Default enabled state should be false.");
        assertNull(replicationDescriptor.getCronExp(), "Default cron exp should be null.");
        assertFalse(replicationDescriptor.isSyncDeletes(), "Default replication deletes state should be true.");
        assertTrue(replicationDescriptor.isSyncProperties(), "Default replication properties state should be true.");
        assertNull(replicationDescriptor.getPathPrefix(), "Default path prefix should be null.");
        assertNull(replicationDescriptor.getRepoKey(), "Default repo key should be null.");
    }

    @Test
    public void testBaseSetters() throws Exception {
        T replicationDescriptor = constructDescriptor();
        replicationDescriptor.setEnabled(true);
        replicationDescriptor.setCronExp("0 0/5 * * * ?");
        replicationDescriptor.setSyncDeletes(false);
        replicationDescriptor.setSyncProperties(false);
        replicationDescriptor.setPathPrefix("jojo");
        replicationDescriptor.setRepoKey("koko");

        assertTrue(replicationDescriptor.isEnabled(), "Unexpected enabled state.");
        assertEquals(replicationDescriptor.getCronExp(), "0 0/5 * * * ?", "Unexpected cron exp.");
        assertFalse(replicationDescriptor.isSyncDeletes(), "Unexpected replication deletes state.");
        assertFalse(replicationDescriptor.isSyncProperties(), "Unexpected replication properties state.");
        assertEquals(replicationDescriptor.getPathPrefix(), "jojo", "Unexpected path prefix.");
        assertEquals(replicationDescriptor.getRepoKey(), "koko", "Unexpected repo key.");
    }

    protected abstract T constructDescriptor();
}
