package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.gems;

import org.artifactory.nuget.NuMetaData;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class GemsArtifactInfo extends BaseArtifactInfo {

    private GemsInfo gemsInfo;
    private List<GemsDependency> gemsDependencies;

    public GemsArtifactInfo() {
    }

    public GemsArtifactInfo(NuMetaData nuMetaData) {
        super.setRepoKey(null);
        super.setPath(null);
    }

    public GemsInfo getGemsInfo() {
        return gemsInfo;
    }

    public void setGemsInfo(GemsInfo gemsInfo) {
        this.gemsInfo = gemsInfo;
    }

    public List<GemsDependency> getGemsDependencies() {
        return gemsDependencies;
    }

    public void setGemsDependencies(
            List<GemsDependency> gemsDependencies) {
        this.gemsDependencies = gemsDependencies;
    }
}
