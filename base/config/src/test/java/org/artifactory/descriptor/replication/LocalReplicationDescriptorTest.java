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

import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Noam Y. Tenne
 */
@Test
public class LocalReplicationDescriptorTest extends ReplicationBaseDescriptorTest<LocalReplicationDescriptor> {

    @Test
    public void testDefaultValues() throws Exception {
        LocalReplicationDescriptor replicationDescriptor = constructDescriptor();
        assertNull(replicationDescriptor.getUrl(), "Unexpected default local replication URL.");
        assertNull(replicationDescriptor.getProxy(), "Unexpected default local replication proxy.");
        assertEquals(replicationDescriptor.getSocketTimeoutMillis(), 15000,
                "Unexpected default local replication timeout.");
        assertNull(replicationDescriptor.getUsername(), "Unexpected default local replication username.");
        assertNull(replicationDescriptor.getPassword(), "Unexpected default local replication password.");
        assertFalse(replicationDescriptor.isEnableEventReplication(),
                "Unexpected default enabled event replication state.");
    }

    @Test
    public void testSetters() throws Exception {
        LocalReplicationDescriptor replicationDescriptor = constructDescriptor();
        replicationDescriptor.setUrl("http://asfaf.com");

        ProxyDescriptor proxy = new ProxyDescriptor();
        replicationDescriptor.setProxy(proxy);
        replicationDescriptor.setSocketTimeoutMillis(545454);
        replicationDescriptor.setUsername("momo");
        replicationDescriptor.setPassword("popo");
        replicationDescriptor.setEnableEventReplication(true);

        assertEquals(replicationDescriptor.getUrl(), "http://asfaf.com", "Unexpected local replication URL.");
        assertEquals(replicationDescriptor.getProxy(), proxy, "Unexpected local replication proxy.");
        assertEquals(replicationDescriptor.getSocketTimeoutMillis(), 545454, "Unexpected local replication timeout.");
        assertEquals(replicationDescriptor.getUsername(), "momo", "Unexpected local replication username.");
        assertEquals(replicationDescriptor.getPassword(), "popo", "Unexpected local replication password.");
        assertTrue(replicationDescriptor.isEnableEventReplication(), "Unexpected enabled event replication state.");
    }

    @Override
    protected LocalReplicationDescriptor constructDescriptor() {
        return new LocalReplicationDescriptor();
    }
}
