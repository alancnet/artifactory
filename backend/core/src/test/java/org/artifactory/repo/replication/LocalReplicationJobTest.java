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

package org.artifactory.repo.replication;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.replication.LocalReplicationSettings;
import org.artifactory.addon.replication.LocalReplicationSettingsBuilder;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.security.SecurityService;
import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.schedule.Task;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

/**
 * @author Noam Y. Tenne
 */
public class LocalReplicationJobTest extends ReplicationJobTestBase {

    private static final LocalReplicationJob job = new LocalReplicationJob();
    private LocalReplicationDescriptor replicationDescriptor;
    private LocalReplicationSettings replicationSettings;

    @BeforeClass
    public void setUp() throws Exception {
        ArtifactoryContextThreadBinder.bind(artifactoryContext);

        replicationDescriptor = new LocalReplicationDescriptor();
        replicationDescriptor.setRepoKey("key");
        replicationDescriptor.setEnabled(true);
        replicationDescriptor.setPathPrefix("path");
        replicationDescriptor.setSyncDeletes(true);
        replicationDescriptor.setSyncProperties(true);
        replicationDescriptor.setSocketTimeoutMillis(111111);
        replicationDescriptor.setUrl("http://momo.com");
        replicationDescriptor.setProxy(new ProxyDescriptor());
        replicationDescriptor.setUsername("username");
        replicationDescriptor.setPassword("password");
        replicationDescriptor.setEnableEventReplication(true);

        replicationSettings = new LocalReplicationSettingsBuilder(replicationDescriptor.getRepoPath(),
                replicationDescriptor.getUrl())
                .deleteExisting(replicationDescriptor.isSyncDeletes())
                .includeProperties(replicationDescriptor.isSyncProperties())
                .username(replicationDescriptor.getUsername())
                .password(replicationDescriptor.getPassword())
                .proxyDescriptor(replicationDescriptor.getProxy())
                .socketTimeoutMillis(replicationDescriptor.getSocketTimeoutMillis())
                .build();
    }

    @Test
    public void testContextNotReady() throws Exception {
        expect(artifactoryContext.isReady()).andReturn(false);
        replayMocks();
        job.onExecute(null);
        verifyMocks();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullRepoKey() throws Exception {
        expect(artifactoryContext.isReady()).andReturn(true);
        expect(executionContext.getJobDetail()).andReturn(jobDetail);
        expect(jobDetail.getJobDataMap()).andReturn(jobDataMap);
        replayMocks();
        job.onExecute(null);
        verifyMocks();
    }

    @Test
    public void testExecutionException() throws Exception {
        expect(artifactoryContext.isReady()).andReturn(true);
        expect(executionContext.getJobDetail()).andReturn(jobDetail);
        expect(jobDetail.getJobDataMap()).andReturn(jobDataMap);
        expect(jobDataMap.getString(Task.REPO_KEY)).andReturn("key");
        expect(jobDataMap.getString(Task.PUSH_REPLICATION_URL)).andReturn("http://momo.com");
        expect(jobDataMap.get(ReplicationAddon.TASK_MANUAL_DESCRIPTOR)).andReturn(null);

        CentralConfigDescriptorImpl centralConfig = new CentralConfigDescriptorImpl();
        centralConfig.addLocalReplication(replicationDescriptor);
         expect(configService.getDescriptor()).andReturn(centralConfig);
        expect(artifactoryContext.getCentralConfig()).andReturn(configService);
        expect(artifactoryContext.beanForType(SecurityService.class)).andReturn(securityService);
        securityService.authenticateAsSystem();
        expectLastCall();
        expect(artifactoryContext.beanForType(AddonsManager.class)).andReturn(addonsManager);
        expect(addonsManager.addonByType(ReplicationAddon.class)).andReturn(replicationAddon);
        expect(replicationAddon.performLocalReplication(replicationSettings))
                .andThrow(new IOException());
        securityService.nullifyContext();
        expectLastCall();
        replayMocks();
        job.onExecute(executionContext);
        verifyMocks();
    }

    @Test
    public void testExecution() throws Exception {
        expect(artifactoryContext.isReady()).andReturn(true);
        expect(executionContext.getJobDetail()).andReturn(jobDetail);
        expect(jobDetail.getJobDataMap()).andReturn(jobDataMap);
        expect(jobDataMap.getString(Task.REPO_KEY)).andReturn("key");
        expect(jobDataMap.getString(Task.PUSH_REPLICATION_URL)).andReturn("http://momo.com");
        expect(jobDataMap.get(ReplicationAddon.TASK_MANUAL_DESCRIPTOR)).andReturn(null);

        CentralConfigDescriptorImpl centralConfig = new CentralConfigDescriptorImpl();
        centralConfig.addLocalReplication(replicationDescriptor);
        expect(configService.getDescriptor()).andReturn(centralConfig);
        expect(artifactoryContext.getCentralConfig()).andReturn(configService);
        expect(artifactoryContext.beanForType(SecurityService.class)).andReturn(securityService);
        securityService.authenticateAsSystem();
        expectLastCall();
        expect(artifactoryContext.beanForType(AddonsManager.class)).andReturn(addonsManager);
        expect(addonsManager.addonByType(ReplicationAddon.class)).andReturn(replicationAddon);
        expect(replicationAddon.performLocalReplication(replicationSettings))
                .andReturn(new BasicStatusHolder());
        securityService.nullifyContext();
        expectLastCall();
        replayMocks();
        job.onExecute(executionContext);
        verifyMocks();
    }

    @Test
    public void testManualExecution() throws Exception {
        expect(artifactoryContext.isReady()).andReturn(true);
        expect(executionContext.getJobDetail()).andReturn(jobDetail);
        expect(jobDetail.getJobDataMap()).andReturn(jobDataMap);
        expect(jobDataMap.get(ReplicationAddon.TASK_MANUAL_DESCRIPTOR)).andReturn(replicationDescriptor);

        expect(artifactoryContext.beanForType(SecurityService.class)).andReturn(securityService);
        securityService.authenticateAsSystem();
        expectLastCall();
        expect(artifactoryContext.beanForType(AddonsManager.class)).andReturn(addonsManager);
        expect(addonsManager.addonByType(ReplicationAddon.class)).andReturn(replicationAddon);
        expect(replicationAddon.performLocalReplication(replicationSettings))
                .andReturn(new BasicStatusHolder());
        securityService.nullifyContext();
        expectLastCall();
        replayMocks();
        job.onExecute(executionContext);
        verifyMocks();
    }
}
