package org.artifactory.ui.rest.model.utils.cron;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class CronTime extends BaseModel {

    private String nextTime;

    public CronTime(String nextExecutionTime) {
        this.nextTime = nextExecutionTime;
    }

    public String getNextTime() {
        return nextTime;
    }

    public void setNextTime(String nextTime) {
        this.nextTime = nextTime;
    }
}
