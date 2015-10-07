package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Gidi Shabat
 */
public class PropertyWithPath extends BaseModel{
    private String name;
    private String path;
    private String repoKey;
    private boolean recursive;

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getRepoKey() {
        return repoKey;
    }
}
