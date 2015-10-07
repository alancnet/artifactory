package org.artifactory.ui.rest.service.utils.predefinevalues;

import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.property.PredefinedValue;
import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.utils.predefinevalues.PreDefineValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetPreDefineValuesService implements RestService {

    @Autowired
    private RepositoryService repoService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoKey = request.getQueryParamByKey("repoKey");
        String path = request.getQueryParamByKey("path");
        String name = request.getPathParamByKey("name");
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        PreDefineValues values = new PreDefineValues();
        Map<String, Property> propertyItemMap = createPropertyItemMap(repoPath);
        if(!propertyItemMap.isEmpty()) {
            List<PredefinedValue> predefinedValues = propertyItemMap.get(name).getPredefinedValues();
            List<String> listOfPredefineValuesAsString = new ArrayList<>();
            List<String> selectedValues = new ArrayList<>();
            predefinedValues.forEach(predefinedValue -> {
                if (predefinedValue.isDefaultValue()) {
                    selectedValues.add(predefinedValue.getValue());
                } else {
                    listOfPredefineValuesAsString.add(predefinedValue.getValue());
                }
            });
            values.setSelectedValues(selectedValues);
            values.setPredefinedValues(listOfPredefineValuesAsString);
        }
        response.iModel(values);
    }

    /**
     * create property map by repo path
     * @param repoPath - repo path
     * @return map of properties
     */
    private Map<String, Property> createPropertyItemMap(RepoPath repoPath) {
        Map<String, Property> propertyItemMap = new HashMap<>();
        LocalRepoDescriptor descriptor = repoService.localOrCachedRepoDescriptorByKey(repoPath.getRepoKey());
        List<PropertySet> propertySets = new ArrayList<>(descriptor.getPropertySets());
        for (PropertySet propertySet : propertySets) {
            List<Property> propertyList = propertySet.getProperties();
            for (Property property : propertyList) {
                propertyItemMap.put(propertySet.getName()+"."+property.getName(), property);
            }
        }
        return propertyItemMap;
    }
}
