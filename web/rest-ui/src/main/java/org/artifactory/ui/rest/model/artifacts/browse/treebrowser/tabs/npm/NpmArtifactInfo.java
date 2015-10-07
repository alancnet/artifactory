package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.npm;

import org.artifactory.addon.npm.NpmDependency;
import org.artifactory.addon.npm.NpmInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class NpmArtifactInfo extends BaseArtifactInfo {

    private NpmInfo npmInfo;
    private List<NpmDependency> npmDependencies;

    public NpmInfo getNpmInfo() {
        return npmInfo;
    }

    public void setNpmInfo(NpmInfo npmInfo) {
        this.npmInfo = npmInfo;
    }

    public List<NpmDependency> getNpmDependencies() {
        return npmDependencies;
    }

    public void setNpmDependencies(List<NpmDependency> npmDependencies) {
        this.npmDependencies = npmDependencies;
    }
}
