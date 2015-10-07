package org.artifactory.ui.rest.service.admin.configuration.propertysets;

import org.apache.http.HttpStatus;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.AdminPropertySetModel;
import org.artifactory.ui.rest.resource.admin.configuration.propertysets.PropertySetsResource;
import org.artifactory.util.stream.BiOptional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetConfigPropertySetService implements RestService {

    @Autowired
    CentralConfigService configService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String propName = request.getPathParamByKey(PropertySetsResource.PROP_SET_NAME);
        BiOptional.of(configService.getDescriptor().getPropertySets().stream()
                .filter(propertySet -> propertySet.getName().equals(propName))
                .map(AdminPropertySetModel::new)
                .findFirst())
                .ifPresent(response::iModel)
                .ifNotPresent(() -> response.error("Property set '" + propName + "' does not exists")
                        .responseCode(HttpStatus.SC_NOT_FOUND));
    }
}
