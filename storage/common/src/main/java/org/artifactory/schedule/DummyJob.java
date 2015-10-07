package org.artifactory.schedule;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.security.core.Authentication;

/**
 * @author Chen Keinan
 */

/**
 *  this job is the default job to be used as default on Stop Command annotation
 */
public class DummyJob extends TaskCallback {
    @Override
    protected String triggeringTaskTokenFromWorkContext(JobExecutionContext workContext) {
        return null;
    }

    @Override
    protected Authentication getAuthenticationFromWorkContext(JobExecutionContext callbackContext) {
        return null;
    }

    @Override
    protected boolean isRunOnlyOnMaster(JobExecutionContext jobContext) {
        return false;
    }

    @Override
    protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {

    }
}
