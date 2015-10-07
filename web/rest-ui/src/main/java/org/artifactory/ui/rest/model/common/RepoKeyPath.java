package org.artifactory.ui.rest.model.common;

/**
 * @author Chen Keinan
 */
public class RepoKeyPath {

    private String path;
    private String repoKey;

    RepoKeyPath() {
    }

    public RepoKeyPath(String path, String repoKey) {
        this.path = path;
        this.repoKey = repoKey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }
}
