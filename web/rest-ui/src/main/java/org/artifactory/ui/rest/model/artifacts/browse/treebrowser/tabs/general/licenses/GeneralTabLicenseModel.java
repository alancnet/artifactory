package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.licenses;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.blackduck.BlackDuckAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.license.LicenseInfo;
import org.artifactory.rest.common.model.BaseModel;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @author Dan Feldman
 */
public class GeneralTabLicenseModel extends BaseModel {

    private String name;
    private String url;
    private Boolean approved = null;

    @JsonIgnore
    public static final GeneralTabLicenseModel NOT_FOUND = createNotFound();

    public GeneralTabLicenseModel() {

    }

    public GeneralTabLicenseModel(String name) {
        this.name = name;
        this.url = null;
    }

    /**
     * Constructor for Black Duck licenses retrieved from props
     */
    public static GeneralTabLicenseModel blackDuckOf(String name) {
        GeneralTabLicenseModel model = new GeneralTabLicenseModel();
        model.name = name;
        model.url = getLicenseUrl(name);
        return model;
    }

    /**
     * Constructor for licenseInfo
     */
    public GeneralTabLicenseModel(LicenseInfo licenseInfo) {
        if (licenseInfo.getName().equals(LicenseInfo.UNKNOWN)) {
            this.name = LicenseInfo.UNKNOWN + "(" + licenseInfo.getLongName() + ")";
        } else {
            this.name = licenseInfo.getName();
            this.approved = licenseInfo.isApproved();
            this.url = licenseInfo.getUrl();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    private static String getLicenseUrl(String license) {
        return ContextHelper.get().beanForType(AddonsManager.class).addonByType(BlackDuckAddon.class)
                .getLicenseUrl(license);
    }

    @JsonIgnore
    private static GeneralTabLicenseModel createNotFound() {
        GeneralTabLicenseModel notFound = new GeneralTabLicenseModel();
        notFound.url = "";
        notFound.approved = false;
        notFound.name = LicenseInfo.NOT_FOUND;
        return notFound;
    }

    @JsonIgnore
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeneralTabLicenseModel)) return false;

        GeneralTabLicenseModel that = (GeneralTabLicenseModel) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getUrl() != null ? !getUrl().equals(that.getUrl()) : that.getUrl() != null) return false;
        return !(getApproved() != null ? !getApproved().equals(that.getApproved()) : that.getApproved() != null);

    }

    @JsonIgnore
    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getUrl() != null ? getUrl().hashCode() : 0);
        result = 31 * result + (getApproved() != null ? getApproved().hashCode() : 0);
        return result;
    }
}
