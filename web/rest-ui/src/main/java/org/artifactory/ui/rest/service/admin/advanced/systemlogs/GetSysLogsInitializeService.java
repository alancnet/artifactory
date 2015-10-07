package org.artifactory.ui.rest.service.admin.advanced.systemlogs;

import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.advanced.systemlogs.SystemLogsInitialize;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Lior Hasson
 */

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetSysLogsInitializeService implements RestService {
    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        // update response with system log comboBox
        response.iModel(new SystemLogsInitialize());
    }
}
