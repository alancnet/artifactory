package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import com.google.common.collect.Lists;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.PropertySetNameModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.info.RepositoryDefaultValuesModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteAdvancedRepositoryConfigModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetDefaultRepositoryValues implements RestService<RepositoryDefaultValuesModel> {

    @Autowired
    CentralConfigService configService;

    @Override
    public void execute(ArtifactoryRestRequest<RepositoryDefaultValuesModel> request, RestResponse response) {
        RepositoryDefaultValuesModel defaultValues = new RepositoryDefaultValuesModel();
        //Add artifactory prop set as default
        configService.getDescriptor().getPropertySets().stream()
                .filter(propertySet -> propertySet.getName().equals(PropertySet.ARTIFACTORY_RESERVED_PROP_SET))
                .findAny().ifPresent(
                artPropSet -> {
                    ((LocalAdvancedRepositoryConfigModel) defaultValues.getDefaultModels()
                            .get("localAdvanced")).setPropertySets(Lists.newArrayList(new PropertySetNameModel(artPropSet)));
                    ((RemoteAdvancedRepositoryConfigModel) defaultValues.getDefaultModels()
                            .get("remoteAdvanced")).setPropertySets(Lists.newArrayList(new PropertySetNameModel(artPropSet)));
                });
        response.iModel(defaultValues);
    }
}
