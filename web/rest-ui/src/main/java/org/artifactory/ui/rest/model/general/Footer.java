package org.artifactory.ui.rest.model.general;

import org.artifactory.descriptor.message.SystemMessageDescriptor;
import org.artifactory.rest.common.model.BaseModel;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Chen Keinan
 */
public class Footer extends BaseModel {
    private String versionInfo;
    private String buildNumber;
    private String licenseInfo;
    private String copyRights;
    private String copyRightsUrl;
    private boolean isAol;
    private String versionID;
    private boolean globalRepoEnabled;
    private boolean userLogo;
    private String logoUrl;
    private String serverName;
    private boolean helpLinksEnabled;
    private boolean systemMessageEnabled;
    private String systemMessageTitle;
    private String systemMessageTitleColor;
    private String systemMessage;
    private boolean showSystemMessageOnAllPages;

    public Footer(String licenseInfo, String versionInfo, String copyRights, String copyRightsUrl,
            String buildNumber, boolean isAol, boolean isGlobalRepoEnabled, String versionID, boolean userLogo,
            String logoUrl, String serverName, SystemMessageDescriptor systemMessageDescriptor, boolean helpLinksEnabled) {
        this.licenseInfo = licenseInfo;
        this.versionInfo = versionInfo;
        this.copyRights = copyRights;
        this.copyRightsUrl = copyRightsUrl;
        this.buildNumber = buildNumber;
        this.isAol = isAol;
        this.globalRepoEnabled = isGlobalRepoEnabled;
        this.versionID = versionID;
        this.userLogo = userLogo;
        this.logoUrl = logoUrl;
        this.serverName = serverName;
        this.systemMessageEnabled = systemMessageDescriptor.isEnabled();
        this.systemMessageTitle = systemMessageDescriptor.getTitle();
        this.systemMessageTitleColor = systemMessageDescriptor.getTitleColor();
        this.systemMessage = systemMessageDescriptor.getMessage();
        this.showSystemMessageOnAllPages = systemMessageDescriptor.isShowOnAllPages();
        this.helpLinksEnabled = helpLinksEnabled;
    }

    public String getLicenseInfo() {
        return licenseInfo;
    }

    public void setLicenseInfo(String licenseInfo) {
        this.licenseInfo = licenseInfo;
    }

    public String getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(String versionInfo) {
        this.versionInfo = versionInfo;
    }

    public String getCopyRights() {
        return copyRights;
    }

    public void setCopyRights(String copyRights) {
        this.copyRights = copyRights;
    }

    public String getCopyRightsUrl() {
        return copyRightsUrl;
    }

    public void setCopyRightsUrl(String copyRightsUrl) {
        this.copyRightsUrl = copyRightsUrl;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    @JsonProperty("isAol")
    public boolean isAol() {
        return isAol;
    }

    public String getVersionID() {
        return versionID;
    }

    public void setVersionID(String versionID) {
        this.versionID = versionID;
    }

    public boolean isGlobalRepoEnabled() {
        return globalRepoEnabled;
    }

    public boolean isUserLogo() {
        return userLogo;
    }

    public void setUserLogo(boolean userLogo) {
        this.userLogo = userLogo;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public boolean isSystemMessageEnabled() {
        return systemMessageEnabled;
    }

    public String getSystemMessageTitle() {
        return systemMessageTitle;
    }

    public String getSystemMessageTitleColor() {
        return systemMessageTitleColor;
    }

    public String getSystemMessage() {
        return systemMessage;
    }

    public boolean isShowSystemMessageOnAllPages() {
        return showSystemMessageOnAllPages;
    }

    public boolean isHelpLinksEnabled() {
        return helpLinksEnabled;
    }
}
