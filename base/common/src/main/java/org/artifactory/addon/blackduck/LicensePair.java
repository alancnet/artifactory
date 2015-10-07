package org.artifactory.addon.blackduck;

/**
 * @author Chen Keinan
 */
public class LicensePair {

    private String licenseName;
    private String licenseUrl;

    public LicensePair(String licenseName, String licenseLink) {
        this.licenseName = licenseName;
        this.licenseUrl = licenseLink;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }
}
