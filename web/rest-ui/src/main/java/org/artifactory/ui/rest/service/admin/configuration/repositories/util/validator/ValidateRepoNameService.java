package org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator;

import com.google.common.collect.Maps;
import org.apache.http.HttpStatus;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.exception.RepoConfigException;
import org.artifactory.ui.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Provides the entire validation logic for a repository key for the ui in a single REST call
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ValidateRepoNameService implements RestService {

    @Autowired
    RepoConfigValidator repoConfigValidator;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoKey = RequestUtils.getRepoKeyFromRequest(request);
        try {
            repoConfigValidator.validateRepoName(repoKey);
        } catch (RepoConfigException e) {
            Map<String, String> errorMap = Maps.newHashMap();
            errorMap.put("error", e.getMessage());
            response.iModel(errorMap);
        }
        //Always return OK for ui validation
        response.responseCode(HttpStatus.SC_OK);
    }
}