package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.nugetinfo;

import org.artifactory.nuget.NuMetaData;

/**
 * @author Chen Keinan
 */
public class NugetGeneralInfo {

    private String iconUrl;
    private String id;
    private String pkgTitle;
    private String version;
    private String authors;
    private String owners;
    private String licenseUrl;
    private String languages;
    private boolean requireLicenseAcceptance;
    private String summary;
    private String projectUrl;
    private String description;
    private String tags;
    private String releaseNotes;
    private String copyright;

    public NugetGeneralInfo(NuMetaData nuMetaData) {
        this.id = nuMetaData.getId();
        this.pkgTitle = nuMetaData.getTitle();
        this.authors = nuMetaData.getAuthors();
        this.version = nuMetaData.getVersion();
        this.owners = nuMetaData.getOwners();
        this.licenseUrl = nuMetaData.getLicenseUrl();
        this.requireLicenseAcceptance = nuMetaData.isRequireLicenseAcceptance();
        this.summary = nuMetaData.getSummary();
        this.tags = nuMetaData.getTags();
        this.languages = nuMetaData.getLanguage();
        this.projectUrl = nuMetaData.getProjectUrl();
        this.iconUrl = nuMetaData.getIconUrl();
        this.description = nuMetaData.getDescription();
        this.releaseNotes = nuMetaData.getReleaseNotes();
        this.copyright = nuMetaData.getCopyright();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPkgTitle() {
        return pkgTitle;
    }

    public void setPkgTitle(String pkgTitle) {
        this.pkgTitle = pkgTitle;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getOwners() {
        return owners;
    }

    public void setOwners(String owners) {
        this.owners = owners;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public boolean isRequireLicenseAcceptance() {
        return requireLicenseAcceptance;
    }

    public void setRequireLicenseAcceptance(boolean requireLicenseAcceptance) {
        this.requireLicenseAcceptance = requireLicenseAcceptance;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
}
