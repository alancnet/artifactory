package org.artifactory.addon.gems;

/**
 * @author Chen Keinan
 */
public class ArtifactGemsInfo {
    private String name;
    private String version;
    private String platform;
    private String authors;
    private String info;
    private GemsDependsInfo dependencies;
    private String homepage;
    private String summary;
    private String license;

    public ArtifactGemsInfo() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPlatform() {
        return this.platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getAuthors() {
        return this.authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getInfo() {
        return this.info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public GemsDependsInfo getDependencies() {
        return dependencies;
    }

    public void setDependencies(GemsDependsInfo dependencies) {
        this.dependencies = dependencies;
    }

    public String getHomepage() {
        return this.homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getLicense() {
        return this.license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}
