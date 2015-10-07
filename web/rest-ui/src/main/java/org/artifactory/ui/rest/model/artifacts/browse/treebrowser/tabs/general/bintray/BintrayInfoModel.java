package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.bintray;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Shay Yaakov
 */
public class BintrayInfoModel extends BaseModel {

    private String name;
    private String nameLink;
    private String description;
    private String latestVersion;
    private String latestVersionLink;
    private String iconURL;
    private String errorMessage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameLink() {
        return nameLink;
    }

    public void setNameLink(String nameLink) {
        this.nameLink = nameLink;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getLatestVersionLink() {
        return latestVersionLink;
    }

    public void setLatestVersionLink(String latestVersionLink) {
        this.latestVersionLink = latestVersionLink;
    }

    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
