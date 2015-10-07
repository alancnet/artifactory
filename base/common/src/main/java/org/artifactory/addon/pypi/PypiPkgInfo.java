package org.artifactory.addon.pypi;

import javax.annotation.Nullable;

/**
 * @author Chen Keinan
 */
public class PypiPkgInfo {

    private String name;
    private String version;
    private String platform;
    private String supportedPlatform;
    private String summary;
    @Nullable
    private String description;
    @Nullable
    private String keywords;
    @Nullable
    private String homepage;
    private String downloadUrl;
    @Nullable
    private String author;
    private String authorEmail;
    private String license;

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

    public String getSupportedPlatform() {
        return supportedPlatform;
    }

    public void setSupportedPlatform(String supportedPlatform) {
        this.supportedPlatform = supportedPlatform;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Nullable
    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(@Nullable String keywords) {
        this.keywords = keywords;
    }

    @Nullable
    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(@Nullable String homepage) {
        this.homepage = homepage;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Nullable
    public String getAuthor() {
        return author;
    }

    public void setAuthor(@Nullable String author) {
        this.author = author;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}
