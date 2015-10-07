package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import org.apache.http.HttpStatus;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Serves the remote search tab, when verifying if a  JCenter repo is configured in Artifactory.
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class IsJcenterConfiguredService implements RestService {
    private static final String DEFAULT_JCENTER_URL = "jcenter.bintray.com";

    @Autowired
    private CentralConfigService configService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        if (configService.getDescriptor().getRemoteRepositoriesMap().values().stream()
                .filter(remoteRepoDescriptor -> remoteRepoDescriptor.getUrl().contains(DEFAULT_JCENTER_URL))
                .findAny().isPresent()) {
            response.responseCode(HttpStatus.SC_OK);
        } else {
            response.responseCode(HttpStatus.SC_NOT_FOUND);
        }
    }
}