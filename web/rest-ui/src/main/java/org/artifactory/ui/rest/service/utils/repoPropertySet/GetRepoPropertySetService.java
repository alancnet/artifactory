package org.artifactory.ui.rest.service.utils.repoPropertySet;

import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.PropertiesArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.RepoProperty;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.RepoPropertySet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
public class GetRepoPropertySetService implements RestService {

    @Autowired
    private RepositoryService repoService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String path = request.getQueryParamByKey("path");
        String repoKey = request.getQueryParamByKey("repoKey");
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        List<RestModel> propertyItemList = createPropertyItemMap(repoPath);
        response.iModelList(propertyItemList);
    }

    /**
     * Prepares a list of Property Items to add to the property drop-down component
     */

    /**
     * Prepares a list of Property Items to add to the property drop-down component
     * * @param repoPath - repo path
     * @return list of properties artifact info
     */
    private List<RestModel> createPropertyItemMap(RepoPath repoPath) {
        List<RestModel> propertyItems = new ArrayList<>();
        LocalRepoDescriptor descriptor = repoService.localOrCachedRepoDescriptorByKey(repoPath.getRepoKey());
        List<PropertySet> propertySets = new ArrayList<>(descriptor.getPropertySets());
        for (PropertySet propertySet : propertySets) {
            List<Property> propertyList = propertySet.getProperties();
            for (Property property : propertyList) {
                PropertySet repoPropertySet = new RepoPropertySet();
                repoPropertySet.setName(propertySet.getName());
                Property repoProperty = new RepoProperty();
                repoProperty.setName(property.getName());
                PropertiesArtifactInfo item = new PropertiesArtifactInfo(repoPropertySet, repoProperty);
                item.setPropertyType(property.getPropertyType().name());
                propertyItems.add(item);
            }
        }
        return propertyItems;
    }
}
