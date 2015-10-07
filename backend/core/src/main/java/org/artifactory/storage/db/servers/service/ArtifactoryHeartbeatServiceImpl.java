package org.artifactory.storage.db.servers.service;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.schedule.JobCommand;
import org.artifactory.schedule.TaskBase;
import org.artifactory.schedule.TaskService;
import org.artifactory.schedule.TaskUser;
import org.artifactory.schedule.TaskUtils;
import org.artifactory.schedule.quartz.QuartzCommand;
import org.artifactory.spring.Reloadable;
import org.artifactory.spring.ReloadableBean;
import org.artifactory.state.model.ArtifactoryStateManager;
import org.artifactory.version.CompoundVersionDetails;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * author: gidis
 */
@Service
@Reloadable(beanClass = ArtifactoryHeartbeatService.class,
        initAfter = {TaskService.class, ArtifactoryStateManager.class})
public class ArtifactoryHeartbeatServiceImpl implements ArtifactoryHeartbeatService, ReloadableBean {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryHeartbeatServiceImpl.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private ArtifactoryServersCommonService artifactoryServersService;

    @Override
    public void init() {
        TaskBase heartbeatJob = TaskUtils.createRepeatingTask(HeartbeatJob.class,
                TimeUnit.SECONDS.toMillis(ConstantValues.haHeartbeatIntervalSecs.getLong()),
                TimeUnit.SECONDS.toMillis(ConstantValues.haHeartbeatIntervalSecs.getLong()));
        taskService.startTask(heartbeatJob, false);
    }

    @Override
    public void updateHeartbeat() {
        try {
            String serverId = ContextHelper.get().getServerId();
            long heartbeat = System.currentTimeMillis();
            log.debug("Updating heartbeat for {} [{}]", serverId, heartbeat);
            artifactoryServersService.updateArtifactoryServerHeartbeat(serverId, heartbeat);
            log.debug("Updated heartbeat for {} [{}]", serverId, heartbeat);
        } catch (Exception e) {
            log.error("Failed to update heartbeat", e);
        }
    }

    @JobCommand(singleton = true, runOnlyOnPrimary = false,
            schedulerUser = TaskUser.SYSTEM, manualUser = TaskUser.SYSTEM)
    public static class HeartbeatJob extends QuartzCommand {
        @Override
        protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {
            ContextHelper.get().beanForType(ArtifactoryHeartbeatService.class).updateHeartbeat();
        }
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
    }

    @Override
    public void destroy() {
    }

}
