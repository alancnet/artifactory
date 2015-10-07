package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.gems;

import org.artifactory.addon.gems.ArtifactGemsInfo;

/**
 * @author Chen Keinan
 */
public class GemsInfo {

    private String name;
    private String version;
    private String platform;
    private String summary;
    private String authors;
    private String homepage;
    private String repositoryPath;
    private String description;

    public GemsInfo(ArtifactGemsInfo gemsInfo, String repoKey, String path) {
        this.version = gemsInfo.getVersion();
        this.homepage = gemsInfo.getHomepage();
        this.summary = gemsInfo.getSummary();
        this.name = gemsInfo.getName();
        this.platform = gemsInfo.getPlatform();
        this.authors = gemsInfo.getAuthors();
        this.description = gemsInfo.getInfo();
        this.repositoryPath = repoKey + ":" + path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
