package org.artifactory.ui.rest.service.admin.configuration.propertysets;

import org.apache.http.HttpStatus;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.AdminPropertiesModel;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.AdminPropertySetModel;
import org.artifactory.util.stream.BiOptional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static org.artifactory.ui.rest.resource.admin.configuration.propertysets.PropertySetsResource.PROP_SET_NAME;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdatePropertySetService implements RestService<AdminPropertySetModel> {

    @Autowired
    CentralConfigService configService;

    @Override
    public void execute(ArtifactoryRestRequest<AdminPropertySetModel> request, RestResponse response) {
        AdminPropertySetModel newSet = request.getImodel();
        MutableCentralConfigDescriptor descriptor = configService.getMutableDescriptor();
        newSet.setName(request.getPathParamByKey(PROP_SET_NAME)); //Force propSet name according to endpoint
        BiOptional.of(descriptor.getPropertySets().stream()
                .filter(prop -> prop.getName().equals(newSet.getName()))
                .findFirst())
                .ifPresent(toUpdate -> updatePropertySet(descriptor, toUpdate, newSet, response))
                .ifNotPresent(() -> response.error("Property set '" + newSet.getName() + "' does not exist")
                        .responseCode(HttpStatus.SC_NOT_FOUND));
    }

    private void updatePropertySet(MutableCentralConfigDescriptor descriptor, PropertySet toUpdate,
            AdminPropertySetModel newSet, RestResponse artifactoryResponse) {
        //Unfortunately need to convert model to property
        toUpdate.setProperties(newSet.getProperties().stream()
                .map(AdminPropertiesModel::toProperty)
                .collect(Collectors.toList()));
        configService.saveEditedDescriptorAndReload(descriptor);
        artifactoryResponse.info("Successfully updated property set '" + toUpdate.getName() + "'");
    }
}