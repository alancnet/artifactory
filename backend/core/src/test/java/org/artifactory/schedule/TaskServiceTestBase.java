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

package org.artifactory.schedule;

import ch.qos.logback.classic.Level;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddonsImpl;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.test.TestUtils;
import org.artifactory.test.mock.MockUtils;
import org.easymock.EasyMock;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.annotation.Nullable;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author yoavl
 */
@Test(enabled = false)
public class TaskServiceTestBase extends ArtifactoryHomeBoundTest {

    private static final boolean HIGH_DEBUG = false;
    protected ArtifactoryHomeTaskTestStub artifactoryHome;
    protected TaskServiceImpl taskService;

    @Override
    protected ArtifactoryHomeTaskTestStub getOrCreateArtifactoryHomeStub() {
        if (artifactoryHome == null) {
            artifactoryHome = new ArtifactoryHomeTaskTestStub();
            artifactoryHome.setMimeTypes(mimeTypes);
        }
        return artifactoryHome;
    }

    @BeforeClass
    public void setupTaskService() throws Exception {
        /*LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.getLogger("org.artifactory.schedule").setLevel(Level.DEBUG);*/

        InternalArtifactoryContext context = MockUtils.getThreadBoundedMockContext();

        EasyMock.expect(context.getArtifactoryHome()).andReturn(getOrCreateArtifactoryHomeStub()).anyTimes();
        EasyMock.expect(context.isOffline()).andReturn(false).anyTimes();
        //Create a config mock
        CentralConfigService cc = EasyMock.createMock(CentralConfigService.class);
        EasyMock.expect(context.getCentralConfig()).andReturn(cc).anyTimes();
        CentralConfigDescriptor ccd = EasyMock.createMock(CentralConfigDescriptor.class);
        EasyMock.expect(
                ccd.getLocalRepositoriesMap())
                .andReturn(Maps.<String, LocalRepoDescriptor>newLinkedHashMap()).anyTimes();
        EasyMock.expect(
                ccd.getRemoteRepositoriesMap())
                .andReturn(Maps.<String, RemoteRepoDescriptor>newLinkedHashMap()).anyTimes();
        EasyMock.expect(
                ccd.getVirtualRepositoriesMap())
                .andReturn(Maps.<String, VirtualRepoDescriptor>newLinkedHashMap()).anyTimes();
        EasyMock.expect(cc.getDescriptor()).andReturn(ccd).anyTimes();

        //Put the task service into the context
        taskService = new TaskServiceImpl();
        taskService.onContextReady();
        EasyMock.expect(context.getTaskService()).andReturn(taskService).anyTimes();

        AddonsManager addonsManager = new OssAddonsManager();
        TestUtils.setField(addonsManager, "context", context);
        EasyMock.expect(context.beanForType(AddonsManager.class)).andReturn(addonsManager).anyTimes();
        TestUtils.setField(taskService, "addonsManager", addonsManager);
        CoreAddonsImpl coreAddons = new CoreAddonsImpl();
        EasyMock.expect(context.beanForType(HaCommonAddon.class)).andReturn(coreAddons).anyTimes();

        //Put the scheduler into the context
        try {
            super.bindArtifactoryHome();
            ArtifactorySchedulerFactoryBean schedulerFactory = new ArtifactorySchedulerFactoryBean();
            schedulerFactory.setTaskExecutor(new CachedThreadPoolTaskExecutor());
            schedulerFactory.afterPropertiesSet();
            Scheduler scheduler = schedulerFactory.getObject();
            EasyMock.expect(context.beanForType(Scheduler.class)).andReturn(scheduler).anyTimes();
            schedulerFactory.setApplicationContext(context);

            //Charge the mocks
            EasyMock.replay(context, cc, ccd);
            schedulerFactory.start();
        } finally {
            super.unbindArtifactoryHome();
        }
    }

    @BeforeClass
    public void setEnvironment() throws Exception {
        if (HIGH_DEBUG) {
            TestUtils.setLoggingLevel("org.artifactory.schedule", Level.DEBUG);
            TestUtils.setLoggingLevel("org.artifactory.schedule.TaskBase", Level.TRACE);
            TestUtils.setLoggingLevel("org.artifactory.schedule.TaskCallback", Level.TRACE);
        } else {
            TestUtils.setLoggingLevel("org.artifactory.schedule", Level.INFO);
        }
        getOrCreateArtifactoryHomeStub().setProperty(ConstantValues.taskCompletionLockTimeoutRetries, "3");
        getOrCreateArtifactoryHomeStub().setProperty(ConstantValues.locksTimeoutSecs, "5");
    }

    @AfterMethod(enabled = HIGH_DEBUG, lastTimeOnly = true)
    public void assertNoActiveTasks() throws SchedulerException {
        List<TaskBase> activeTasks = taskService.getActiveTasks(new Predicate<Task>() {
            @Override
            public boolean apply(@Nullable Task input) {
                return true;
            }
        });
        assertTrue(activeTasks.isEmpty(), "Active tasks after test method finished: " + activeTasks);
        Scheduler scheduler = ContextHelper.get().beanForType(Scheduler.class);
        assertEquals(scheduler.getJobKeys(GroupMatcher.<JobKey>anyGroup()).size(), 0);
    }
}