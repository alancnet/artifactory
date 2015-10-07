package org.artifactory.nuget;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class NuMetaData {

    private String id;
    private String version;
    private String title;
    private String authors;
    private String owners;
    private String licenseUrl;
    private String projectUrl;
    private String iconUrl;
    private boolean requireLicenseAcceptance;
    private String description;
    private String summary;
    private String language;
    private String tags;
    private String releaseNotes;
    private String copyright;
    private List dependencies;
    private List<NuSpecFrameworkAssembly> frameworkAssemblies;
    private List references;


    public NuMetaData(String id, String version, String title, String authors, String owners, String licenseUrl,
            String projectUrl, String iconUrl, boolean requireLicenseAcceptance, String description, String summary,
            String language, String tags, String releaseNotes, String copyright, List dependencies, List references,
            List<NuSpecFrameworkAssembly> frameworkAssemblies) {
        this.id = id;
        this.version = version;
        this.title = title;
        this.authors = authors;
        this.owners = owners;
        this.licenseUrl = licenseUrl;
        this.projectUrl = projectUrl;
        this.iconUrl = iconUrl;
        this.requireLicenseAcceptance = requireLicenseAcceptance;
        this.description = description;
        this.summary = summary;
        this.language = language;
        this.tags = tags;
        this.releaseNotes = releaseNotes;
        this.copyright = copyright;
        this.dependencies = dependencies;
        this.references = references;
        this.frameworkAssemblies = frameworkAssemblies;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public boolean isRequireLicenseAcceptance() {
        return requireLicenseAcceptance;
    }

    public void setRequireLicenseAcceptance(boolean requireLicenseAcceptance) {
        this.requireLicenseAcceptance = requireLicenseAcceptance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
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

    public List getDependencies() {
        return dependencies;
    }

    public void setDependencies(List dependencies) {
        this.dependencies = dependencies;
    }

    public List getReferences() {
        return references;
    }

    public void setReferences(List references) {
        this.references = references;
    }

    public List<NuSpecFrameworkAssembly> getFrameworkAssemblies() {
        return frameworkAssemblies;
    }

    public void setFrameworkAssemblies(List<NuSpecFrameworkAssembly> frameworkAssemblies) {
        this.frameworkAssemblies = frameworkAssemblies;
    }
}
