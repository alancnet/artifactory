package org.artifactory.addon.blackduck;

/**
 * @author Chen Keinan
 */
public class BlackduckInfo {

    private String name;
    private String version;
    private String componentId;
    private String extComponentId;
    private String kbComponentId;
    private String kbReleaseId;
    private String homepage;
    private String description;
    private boolean isCatalogComponent;
    private String componentLink;
    private String licenseLink;

    public BlackduckInfo(String name, String version, String componentId, String kbComponentId,
            String kbReleaseId, String homepage, String description, boolean component) {
        this.name = name;
        this.version = version;
        this.componentId = componentId;
        this.kbComponentId = kbComponentId;
        this.kbReleaseId = kbReleaseId;
        this.homepage = homepage;
        this.description = description;
        this.isCatalogComponent = component;
    }

    public BlackduckInfo() {
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

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getKbComponentId() {
        return kbComponentId;
    }

    public void setKbComponentId(String kbComponentId) {
        this.kbComponentId = kbComponentId;
    }

    public String getKbReleaseId() {
        return kbReleaseId;
    }

    public void setKbReleaseId(String kbReleaseId) {
        this.kbReleaseId = kbReleaseId;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCatalogComponent() {
        return isCatalogComponent;
    }

    public void setIsCatalogComponent(boolean isCatalogComponent) {
        this.isCatalogComponent = isCatalogComponent;
    }

    public String getComponentLink() {
        return componentLink;
    }

    public void setComponentLink(String compponentLink) {
        this.componentLink = compponentLink;
    }

    public String getLicenseLink() {
        return licenseLink;
    }

    public void setLicenseLink(String licenseLink) {
        this.licenseLink = licenseLink;
    }

    public String getExtComponentId() {
        return extComponentId;
    }

    public void setExtComponentId(String extComponentId) {
        this.extComponentId = extComponentId;
    }
}
