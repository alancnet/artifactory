package org.artifactory.addon.npm;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class NpmMetadataInfo {

    private NpmInfo npmInfo;
    private List<NpmDependency> npmDependencies;

    public NpmMetadataInfo(NpmInfo npmInfo, List<NpmDependency> dependencies) {
        this.npmInfo = npmInfo;
        this.npmDependencies = dependencies;
    }

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
