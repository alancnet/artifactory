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
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.spring.InternalArtifactoryContext;
import org.easymock.EasyMock;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.testng.annotations.BeforeMethod;

/**
 * @author Noam Y. Tenne
 */
public abstract class ReplicationJobTestBase {

    protected static final InternalArtifactoryContext artifactoryContext = EasyMock.createMock(
            InternalArtifactoryContext.class);
    protected static final JobExecutionContext executionContext = EasyMock.createMock(JobExecutionContext.class);
    protected static final JobDetail jobDetail = EasyMock.createMock(JobDetail.class);
    protected static final JobDataMap jobDataMap = EasyMock.createMock(JobDataMap.class);
    protected static final CentralConfigService configService = EasyMock.createMock(CentralConfigService.class);
    protected static final SecurityService securityService = EasyMock.createMock(SecurityService.class);
    protected static final AddonsManager addonsManager = EasyMock.createMock(AddonsManager.class);
    protected static final ReplicationAddon replicationAddon = EasyMock.createMock(ReplicationAddon.class);
    protected static final RepositoryService repositoryService = EasyMock.createMock(RepositoryService.class);

    @BeforeMethod
    public void reset() throws Exception {
        EasyMock.reset(artifactoryContext, executionContext, jobDetail, jobDataMap, configService, securityService,
                addonsManager, replicationAddon, repositoryService);
    }

    protected void replayMocks() {
        EasyMock.replay(artifactoryContext, executionContext, jobDetail, jobDataMap, configService, securityService,
                addonsManager, replicationAddon, repositoryService);
    }

    protected void verifyMocks() {
        EasyMock.verify(artifactoryContext, executionContext, jobDetail, jobDataMap, configService, securityService,
                addonsManager, replicationAddon, repositoryService);
    }
}
