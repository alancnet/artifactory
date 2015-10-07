package org.artifactory.ui.rest.model.admin.configuration.licenses;

import org.artifactory.api.license.ArtifactLicenseModel;
import org.artifactory.api.license.LicenseInfo;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Chen Kainan
 */
public class License extends ArtifactLicenseModel implements RestModel {

    private String status;
    License() {
    }

    public License(LicenseInfo licenseInfo) {
        if (licenseInfo != null) {
            super.setApproved(licenseInfo.isApproved());
            super.setComments(licenseInfo.getComments());
            super.setLongName(licenseInfo.getLongName());
            super.setName(licenseInfo.getName());
            super.setRegexp(licenseInfo.getRegexp());
            super.setUrl(licenseInfo.getUrl());
        }
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
