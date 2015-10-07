package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class BaseArtifactInfo extends BaseModel implements IArtifactInfo {

    private String name;
    private String repoKey;
    private String path;

    public BaseArtifactInfo(String name){
        this.name = name;
    }

    public BaseArtifactInfo(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void clearRepoData() {
        this.repoKey = null;
        this.path = null;
    }
}
