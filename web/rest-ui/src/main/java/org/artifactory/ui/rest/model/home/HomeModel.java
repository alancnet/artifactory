package org.artifactory.ui.rest.model.home;

import org.artifactory.rest.common.model.BaseModel;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class HomeModel extends BaseModel {

    private long artifacts;
    private String version;
    private String latestRelease;
    private String upTime;
    private String latestReleaseLink;
    private List<AddonModel> addons;
    private String accountManagementLink;
    private boolean displayAccountManagementLink;


    public long getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(long artifacts) {
        this.artifacts = artifacts;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLatestRelease() {
        return latestRelease;
    }

    public void setLatestRelease(String latestRelease) {
        this.latestRelease = latestRelease;
    }

    public List<AddonModel> getAddons() {
        return addons;
    }

    public void setAddons(List<AddonModel> addons) {
        this.addons = addons;
    }

    public String getLatestReleaseLink() {
        return latestReleaseLink;
    }

    public void setLatestReleaseLink(String latestReleaseLink) {
        this.latestReleaseLink = latestReleaseLink;
    }


    public String getUpTime() {
        return upTime;
    }

    public void setUpTime(String upTime) {
        this.upTime = upTime;
    }

    public String getAccountManagementLink() {
        return accountManagementLink;
    }

    public void setAccountManagementLink(String accountManagementLink) {
        this.accountManagementLink = accountManagementLink;
    }

    public boolean isDisplayAccountManagementLink() {
        return displayAccountManagementLink;
    }

    public void setDisplayAccountManagementLink(boolean displayAccountManagementLink) {
        this.displayAccountManagementLink = displayAccountManagementLink;
    }
}
