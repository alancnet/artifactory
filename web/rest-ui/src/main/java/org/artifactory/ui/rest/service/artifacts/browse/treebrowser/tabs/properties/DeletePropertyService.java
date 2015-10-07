package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.properties;

import org.artifactory.api.properties.PropertiesService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.DeletePropertyModel;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.PropertyWithPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeletePropertyService<T extends DeletePropertyModel> implements RestService<T> {
    private static final Logger log = LoggerFactory.getLogger(DeletePropertyService.class);

    @Autowired
    private PropertiesService propsService;

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        T model = request.getImodel();
        for (PropertyWithPath propertyWithPath : model.getProperties()) {

            String name = propertyWithPath.getName();

            RepoPath path = InternalRepoPathFactory.create(propertyWithPath.getRepoKey(), propertyWithPath.getPath());
            boolean recursive = propertyWithPath.isRecursive();
            // delete property and update response
            deletePropertyAndUpdateResponse(response, name, path, recursive);
        }
        if(model.getProperties().size()>1){
            response.info("Successfully removed "+model.getProperties().size()+" properties");
        }else if(model.getProperties().size()==1){
            response.info("Successfully removed property '" + model.getProperties().get(0).getName() + "'");
        }
    }

    /**
     * delete property from DB and update response feedback
     *
     * @param artifactoryResponse - encapsulate data require to response
     * @param name                - property name
     * @param path                - repo path
     * @param recursive           - if true - delete recursively
     */
    private void deletePropertyAndUpdateResponse(RestResponse artifactoryResponse, String name, RepoPath path,
            boolean recursive) {
        try {
            if (recursive) {
                propsService.deletePropertyRecursively(path, name);
            } else {
                propsService.deleteProperty(path, name);
            }
        } catch (Exception e) {
            log.error("problem with deleting property:" + name);
            artifactoryResponse.error("property " + name + " failed to deleted");
        }
    }
}
