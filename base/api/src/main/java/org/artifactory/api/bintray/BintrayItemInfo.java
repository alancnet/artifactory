package org.artifactory.api.bintray;


import org.artifactory.repo.RepoPath;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Bintray single item info.
 */
public class BintrayItemInfo implements Serializable {
    @JsonProperty(value = "name")
    private String name;
    @JsonProperty(value = "package")
    private String packageName;
    @JsonProperty(value = "version")
    private String version;
    @JsonProperty(value = "repo")
    private String repo;
    @JsonProperty(value = "created")
    private String created;
    @JsonProperty(value = "path")
    private String path;
    @JsonProperty(value = "owner")
    private String owner;

    // Local information
    private boolean cached;
    private RepoPath localRepoPath;


    public String getName() {
        return name;
    }

    public String getPackage() {
        return packageName;
    }

    public String getVersion() {
        return version;
    }

    public String getRepo() {
        return repo;
    }

    public String getCreated() {
        return created;
    }

    public String getPath() {
        return path;
    }

    public boolean getCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }

    public String getOwner() {
        return owner;
    }

    public boolean isCached() {
        return cached;
    }

    public RepoPath getLocalRepoPath() {
        return localRepoPath;
    }

    public void setLocalRepoPath(RepoPath localRepoPath) {
        this.localRepoPath = localRepoPath;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BintrayItemInfo)) {
            return false;
        }

        BintrayItemInfo that = (BintrayItemInfo) o;

        if (!name.equals(that.name)) {
            return false;
        }
        if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null) {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }
        if (repo != null ? !repo.equals(that.repo) : that.repo != null) {
            return false;
        }

        return !(path != null ? !path.equals(that.path) : that.path != null);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (repo != null ? repo.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }
}