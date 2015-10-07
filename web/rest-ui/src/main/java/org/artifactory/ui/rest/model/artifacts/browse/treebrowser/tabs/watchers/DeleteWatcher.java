package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.watchers;

import org.artifactory.rest.common.model.RestModel;

/**
 * @author Gidi Shabat
 */
public class DeleteWatcher  implements RestModel {
    private String name;
    private String repoKey;
    private String path;

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
}
