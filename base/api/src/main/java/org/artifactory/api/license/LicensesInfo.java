/*
 * Copyright 2012 JFrog Ltd. All rights reserved.
 * JFROG PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.artifactory.api.license;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;
import java.util.List;

/**
 * A container licenses class, contains a list of {@link LicenseInfo} each of which represent a license.
 *
 * @author Tomer Cohen
 */
@XStreamAlias(LicensesInfo.ROOT)
public class LicensesInfo implements Serializable {
    public static final String ROOT = "licenses";

    private List<LicenseInfo> licenses;

    public List<LicenseInfo> getLicenses() {
        return licenses;
    }

    public void setLicenses(List<LicenseInfo> licenses) {
        this.licenses = licenses;
    }
}
