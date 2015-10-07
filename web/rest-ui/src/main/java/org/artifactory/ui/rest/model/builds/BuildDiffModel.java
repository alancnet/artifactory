package org.artifactory.ui.rest.model.builds;

import java.util.List;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class BuildDiffModel extends BaseModel {

    private List<ModuleArtifactModel> artifacts;
    private List<ModuleDependencyModel> dependencies;
    private List<BuildPropsModel> props;

    public List<ModuleArtifactModel> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<ModuleArtifactModel> artifacts) {
        this.artifacts = artifacts;
    }

    public List<ModuleDependencyModel> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<ModuleDependencyModel> dependencies) {
        this.dependencies = dependencies;
    }

    public List<BuildPropsModel> getProps() {
        return props;
    }

    public void setProps(List<BuildPropsModel> props) {
        this.props = props;
    }
}
