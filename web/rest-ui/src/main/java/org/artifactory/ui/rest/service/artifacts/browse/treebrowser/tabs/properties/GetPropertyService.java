package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.properties;

import com.google.common.collect.Multiset;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.property.PredefinedValue;
import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.ArtifactProperty;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.PropertiesArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.RepoProperty;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.RepoPropertySet;
import org.artifactory.ui.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetPropertyService implements RestService {

    @Autowired
    private PropertiesService propsService;

    @Autowired
    private RepositoryService repoService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String name = request.getPathParamByKey("name");
        RepoPath repoPath = RequestUtils.getPathFromRequest(request);
        // get single or multi properties
        PropertiesArtifactInfo propertiesTab = getSingleOrMultiPropertiesArtifactInfo(name, repoPath);
        // update response
        response.iModel(propertiesTab);
    }

    /**
     * get single or multi properties (for edit or view)
     * @param name - property name
     * @param repoPath - repo path
     * @return
     */
    private PropertiesArtifactInfo getSingleOrMultiPropertiesArtifactInfo(String name, RepoPath repoPath) {
        List<String> predefineValuesList =  new ArrayList<>();
        PropertiesArtifactInfo propertiesTab = new PropertiesArtifactInfo();
        if (name.length() > 0){
            /// get properties for edit
            getPropertiesForEdit(name, repoPath, predefineValuesList, propertiesTab);
        }
        else{
            // get properties for view
            getMultiProperties(repoPath, propertiesTab);
        }
        return propertiesTab;
    }

    /**
     * get properties for view
     * @param repoPath - repo path
     * @param propertiesTab - encapsulate properties data
     */
    private void getMultiProperties(RepoPath repoPath, PropertiesArtifactInfo propertiesTab) {
        Properties properties = propsService.getProperties(repoPath);
        Multiset<String> keys = properties.keys();
        Map<String,String> propsMap = new HashMap<>();
        List<ArtifactProperty> artifactProperties = new ArrayList<>();
        for (String key : keys){
            final StringBuilder valueBuilder = new StringBuilder();
            if (propsMap.get(key) == null){
                Set<String> values = properties.get(key);
                if (values.size() > 1){
                    values.forEach(multiValue->{
                        valueBuilder.append(multiValue);
                        if (values.size() > 1){
                            valueBuilder.append(";");
                        }
                    });
                    propsMap.put(key,valueBuilder.toString());
                    artifactProperties.add(new ArtifactProperty(key,valueBuilder.toString()));
                }
                else{
                    values.forEach(value -> {
                        propsMap.put(key, value);
                        artifactProperties.add(new ArtifactProperty(key, value));
                    });
                }
            }
        }
        propertiesTab.setArtifactProperties(artifactProperties);
    }

    /**
     * get properties for Edit
     * @param name - property name
     * @param repoPath - repo path
     * @param predefineValuesList = preDefine values
     * @param propertiesTab - encapsulate properties data
     */
    private void getPropertiesForEdit(String name, RepoPath repoPath, List<String> predefineValuesList,
            PropertiesArtifactInfo propertiesTab) {
        Map<String, PropertiesArtifactInfo> propertyItemMap = createPropertyItemMap(repoPath);
        Properties properties = propsService.getProperties(repoPath);
        Set<String> selectedValuesList = properties.get(name);
        if (propertyItemMap.get(name) != null) {
            // multi value
            multiValuePropertySet(name, predefineValuesList, propertiesTab, propertyItemMap, selectedValuesList);
            return;
        }
        propertiesTab.setProperty(new RepoProperty(name));
        propertiesTab.setSelectedValues(selectedValuesList);
    }

    /**
     * create multi value property set
     *
     * @param name                - name
     * @param predefineValuesList - predefine value list
     * @param propertiesTab       - properties holder
     * @param propertyItemMap     - property set map
     * @param selectedValuesList  - select value list
     */
    private void multiValuePropertySet(String name, List<String> predefineValuesList,
            PropertiesArtifactInfo propertiesTab,
            Map<String, PropertiesArtifactInfo> propertyItemMap, Set<String> selectedValuesList) {
        if (selectedValuesList.size() >= 1) {
            List<PredefinedValue> predefinedValues = null;
            if (!propertyItemMap.isEmpty()) {
                predefinedValues = propertyItemMap.get(name).getProperty().getPredefinedValues();
            }
            if (predefinedValues != null) {
                predefinedValues.forEach(predefinedValue -> predefineValuesList.add(predefinedValue.getValue()));
                selectedValuesList.forEach(value -> {
                    predefineValuesList.remove(value);
                });
                propertiesTab.setPredefineValues(predefineValuesList);
            }
        } else {
            propertiesTab.setPredefineValues(null);
        }
        propertiesTab.setParent(new RepoPropertySet(propertyItemMap.get(name).getParent().getName()));
        propertiesTab.setProperty(new RepoProperty(propertyItemMap.get(name).getProperty().getName()));
        propertiesTab.setSelectedValues(selectedValuesList);
        propertiesTab.setPropertyType(propertyItemMap.get(name).getProperty().getPropertyType().name());
    }

    /**
     * create properties map
     * @param repoPath - repo path
     * @return map of properties
     */
    private Map<String, PropertiesArtifactInfo> createPropertyItemMap(RepoPath repoPath) {
        Map<String, PropertiesArtifactInfo> propertyItemMap = new HashMap<>();
        LocalRepoDescriptor descriptor = repoService.localOrCachedRepoDescriptorByKey(repoPath.getRepoKey());
        List<PropertySet> propertySets = new ArrayList<>(descriptor.getPropertySets());
        for (PropertySet propertySet : propertySets) {
            List<Property> propertyList = propertySet.getProperties();
            for (Property property : propertyList) {
                PropertiesArtifactInfo propertiesArtifactInfo =  new PropertiesArtifactInfo(propertySet,property);
                propertyItemMap.put(propertySet.getName()+"."+property.getName(), propertiesArtifactInfo);
            }
        }
        return propertyItemMap;
    }
}
