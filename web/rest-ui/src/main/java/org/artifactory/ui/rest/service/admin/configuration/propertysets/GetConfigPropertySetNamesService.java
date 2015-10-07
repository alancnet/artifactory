package org.artifactory.ui.rest.service.admin.configuration.propertysets;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.PropertySetNameModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetConfigPropertySetNamesService implements RestService {

    private static final String INCOMING_FROM_REPO_FORM = "isRepoForm";

    @Autowired
    CentralConfigService configService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        response.iModelList(configService.getDescriptor().getPropertySets().stream()
                //UI doesn't show the artifactory. prop set and non-visible sets
                .filter(filterPropSetsByCallingScreen(request.getQueryParamByKey(INCOMING_FROM_REPO_FORM)))
                .map(PropertySetNameModel::new)
                .collect(Collectors.toList()));
    }

    /**
     * The Property Set config screen(in admin) does not show the 'artifactory' property set.
     * The repo wizard does show it as a valid selection but not all other invisible prop sets.
     * @return
     */
    private Predicate<PropertySet> filterPropSetsByCallingScreen(String isRepoFormParam) {
        if(Boolean.valueOf(isRepoFormParam)) {

            return propertySet -> propertySet.isVisible()
                    || propertySet.getName().equals(PropertySet.ARTIFACTORY_RESERVED_PROP_SET);
        }
        return propertySet -> !propertySet.getName().equals(PropertySet.ARTIFACTORY_RESERVED_PROP_SET);
    }
}
