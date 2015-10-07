/*
 * Copyright 2012 JFrog Ltd. All rights reserved.
 * JFROG PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.artifactory.api.license;

import java.io.Serializable;

/**
 * @author Tomer Cohen
 */
public class ArtifactLicenseModel implements Serializable {
    public static final String UNAPPROVED = "Unapproved";
    public static final String APPROVED = "Approved";

    private String name;
    private String longName;
    private String url;
    private String comments;
    private String regexp;
    private boolean approved;

    public ArtifactLicenseModel() {
    }

    public ArtifactLicenseModel(LicenseInfo licenseInfo) {
        this.comments = licenseInfo.getComments();
        this.longName = licenseInfo.getLongName();
        this.name = licenseInfo.getName();
        this.url = licenseInfo.getUrl();
        this.regexp = licenseInfo.getRegexp();
        this.approved = licenseInfo.isApproved();
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return isApproved() ? APPROVED : UNAPPROVED;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRegexp() {
        return regexp;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    /**
     * Changes the license status from approved to unapproved or vice versa.
     */
    public void changeStatus() {
        setApproved(!isApproved());
    }

    /**
     * Build a LicenseInfo Object from this model with State.FOUND - use this only when persisting License Info
     */
    public LicenseInfo buildLicenseInfo() {
        LicenseInfo info = new LicenseInfo(name, longName, url);
        info.setApproved(approved);
        info.setComments(comments);
        info.setRegexp(regexp);
        return info;
    }
}
