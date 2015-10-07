package org.artifactory.ui.rest.model.admin.configuration.registerpro;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class ProLicense extends BaseModel {

    public ProLicense() {
    }

    public ProLicense(String[] licenseDetails, String key) {
        if (licenseDetails != null && licenseDetails.length > 0) {
            this.setLicenseTo(licenseDetails[0]);
            this.setValidThough(licenseDetails[1]);
            this.setLicenseType(licenseDetails[2]);
            this.setKey(key);
        }
    }

    private String key;
    private String licenseTo;
    private String validThough;
    private String licenseType;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLicenseTo() {
        return licenseTo;
    }

    public void setLicenseTo(String licenseTo) {
        this.licenseTo = licenseTo;
    }

    public String getValidThough() {
        return validThough;
    }

    public void setValidThough(String validThough) {
        this.validThough = validThough;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }
}
