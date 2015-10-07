package org.artifactory.ui.rest.model.builds;

import org.artifactory.api.build.PublishedModule;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.rest.common.model.RestPaging;

/**
 * @author Chen Keinan
 */
public class BuildModule extends BaseModel implements RestPaging {

    private String moduleId;
    private String numOfArtifacts;
    private String numOfDependencies;

    public BuildModule(PublishedModule module) {
        moduleId = module.getId();
        numOfArtifacts = module.getNumOfArtifact();
        numOfDependencies = module.getNumOfDependencies();
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getNumOfArtifacts() {
        return numOfArtifacts;
    }

    public void setNumOfArtifacts(String numOfArtifacts) {
        this.numOfArtifacts = numOfArtifacts;
    }

    public String getNumOfDependencies() {
        return numOfDependencies;
    }

    public void setNumOfDependencies(String numOfDependencies) {
        this.numOfDependencies = numOfDependencies;
    }
}
