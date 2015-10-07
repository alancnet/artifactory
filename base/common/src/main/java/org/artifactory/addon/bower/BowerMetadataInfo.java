package org.artifactory.addon.bower;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class BowerMetadataInfo {

    private BowerPkgInfo bowerPkgInfo;
    private List<BowerDependencies> bowerDependencies;
    private List<String> mainFiles;
    private List<String> ignoredFiles;

    public BowerPkgInfo getBowerPkgInfo() {
        return bowerPkgInfo;
    }

    public void setBowerPkgInfo(BowerPkgInfo bowerPkgInfo) {
        this.bowerPkgInfo = bowerPkgInfo;
    }

    public List<BowerDependencies> getBowerDependencies() {
        return bowerDependencies;
    }

    public void setBowerDependencies(List<BowerDependencies> bowerDependencies) {
        this.bowerDependencies = bowerDependencies;
    }

    public List<String> getMainFiles() {
        return mainFiles;
    }

    public void setMainFiles(List<String> mainFiles) {
        this.mainFiles = mainFiles;
    }

    public List<String> getIgnoredFiles() {
        return ignoredFiles;
    }

    public void setIgnoredFiles(List<String> ignoredFiles) {
        this.ignoredFiles = ignoredFiles;
    }
}
