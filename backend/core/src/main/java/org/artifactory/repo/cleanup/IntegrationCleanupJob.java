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

package org.artifactory.repo.cleanup;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.schedule.JobCommand;
import org.artifactory.schedule.TaskUser;
import org.artifactory.schedule.quartz.QuartzCommand;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * A quartz command job that periodically cleans unique maven snapshots
 *
 * @author Shay Yaakov
 */
@JobCommand(singleton = true,
        schedulerUser = TaskUser.SYSTEM,
        manualUser = TaskUser.SYSTEM,
        runOnlyOnPrimary = false)
public class IntegrationCleanupJob extends QuartzCommand {

    @Override
    protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {
        ArtifactoryContext artifactoryContext = ContextHelper.get();
        InternalIntegrationCleanupService integrationCleaner = artifactoryContext.beanForType(
                InternalIntegrationCleanupService.class);
        integrationCleaner.clean();
    }
}
