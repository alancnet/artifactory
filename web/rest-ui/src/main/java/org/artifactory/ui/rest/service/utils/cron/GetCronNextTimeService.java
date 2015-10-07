package org.artifactory.ui.rest.service.utils.cron;

import org.apache.commons.lang.StringUtils;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.utils.cron.CronTime;
import org.artifactory.ui.rest.service.admin.configuration.repositories.replication.ReplicationConfigService;
import org.artifactory.ui.utils.CronUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetCronNextTimeService implements RestService {
    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        // get next cron time
        CronTime cronTime = getCronTime(request, response);
        // update response
        response.iModel(cronTime);
    }

    /**
     * get cron time model
     *
     * @param request  - encapsulate data related to request
     * @param response - encapsulate data require for response
     * @return cron time model
     */
    private CronTime getCronTime(ArtifactoryRestRequest request, RestResponse response) {
        CronTime cronTime = null;
        String cronExp = request.getQueryParamByKey("cron");
        String nextExecutionTime = getNextRunTime(cronExp, response);
        if (nextExecutionTime != null) {
            if (Boolean.valueOf(request.getQueryParamByKey(ReplicationConfigService.IS_REPLICATION_QUERY_PARAM))
                    && CronUtils.isCronIntervalLessThanMinimum(cronExp)) {
                response.error("shortCron");
            }
            cronTime = new CronTime(nextExecutionTime);
        }
        return cronTime;
    }

    /**
     * return next execution time from cron exp
     *
     * @param cronExpression      - cron expression
     * @param artifactoryResponse - encapsulate data require for response
     * @return next execution time
     */
    private String getNextRunTime(String cronExpression, RestResponse artifactoryResponse) {
        if (StringUtils.isEmpty(cronExpression)) {
            artifactoryResponse.error("emptyCron");
        }
        if (CronUtils.isValid(cronExpression)) {
            Date nextExecution = CronUtils.getNextExecution(cronExpression);
            if (nextExecution != null) {
                return formatDate(nextExecution);
            } else {
                artifactoryResponse.error("pastCron");
            }
        }
        artifactoryResponse.error("invalidCron");
        return null;
    }

    /**
     * format date to String
     *
     * @param nextRunDate next execution time in date format
     * @return next execution time as String
     */
    private String formatDate(Date nextRunDate) {
        return nextRunDate.toString();
    }
}
