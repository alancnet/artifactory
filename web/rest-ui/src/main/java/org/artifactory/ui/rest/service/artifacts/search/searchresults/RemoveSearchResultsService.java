package org.artifactory.ui.rest.service.artifacts.search.searchresults;

import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RemoveSearchResultsService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(RemoveSearchResultsService.class);

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {

        String searchName = request.getQueryParamByKey("name");
        try {
            RequestUtils.removeResultsToRequest(searchName, request.getServletRequest());
            response.info("Stash successfully cleared");
        } catch (Exception e) {
            response.error("no result with name:" + searchName + " found");
            log.error("no result with name:" + searchName + " found");
        }
    }
}
