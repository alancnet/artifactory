/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.update.security;

import org.artifactory.update.security.v1.AclsConverter;
import org.artifactory.update.security.v1.UserPermissionsConverter;
import org.artifactory.update.security.v2.RepoPathAclConverter;
import org.artifactory.update.security.v2.SimpleUserConverter;
import org.artifactory.update.security.v3.AclRepoKeysConverter;
import org.artifactory.update.security.v3.AnyRemoteConverter;
import org.artifactory.update.security.v4.AnnotatePermissionXmlConverter;
import org.artifactory.update.security.v6.LdapGroupSettingXmlConverter;
import org.artifactory.update.security.v6.LowercaseUsernameXmlConverter;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.SubConfigElementVersion;
import org.artifactory.version.VersionComparator;
import org.artifactory.version.XmlConverterUtils;
import org.artifactory.version.converter.XmlConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author freds
 * @date Nov 9, 2008
 */
public enum SecurityVersion implements SubConfigElementVersion {
    unsupported(ArtifactoryVersion.v122rc0, ArtifactoryVersion.v125rc6) {
        @Override
        public String convert(String in) {
            throw new IllegalStateException(
                    "Reading security data from backup of Artifactory version older than 1.2.5-rc6 is not supported!");
        }
    },
    v1(ArtifactoryVersion.v125, ArtifactoryVersion.v125u1, new UserPermissionsConverter(), new AclsConverter()),
    v2(ArtifactoryVersion.v130beta1, ArtifactoryVersion.v130beta2,
            new SimpleUserConverter(), new RepoPathAclConverter()),
    v3(ArtifactoryVersion.v130beta3, ArtifactoryVersion.v208, new AnyRemoteConverter(), new AclRepoKeysConverter()),
    v4(ArtifactoryVersion.v210, ArtifactoryVersion.v210, new AnnotatePermissionXmlConverter()),
    v5(ArtifactoryVersion.v211, ArtifactoryVersion.v212),
    // due to a bug 2.1.2 was released with new security version that is the same as 4
    v6(ArtifactoryVersion.v213, ArtifactoryVersion.v213, new LdapGroupSettingXmlConverter(),
            new LowercaseUsernameXmlConverter()),
    v7(ArtifactoryVersion.v220, ArtifactoryVersion.v264),
    v8(ArtifactoryVersion.v265, ArtifactoryVersion.getCurrent());

    private static final String VERSION_ATT = "version=\"";

    private final VersionComparator comparator;
    private final XmlConverter[] xmlConverters;

    /**
     * Represents Artifactory security version. For each change in the security files new security version is created.
     *
     * @param from          Artifactory version this security version started at
     * @param until         Last Artifactory version this security version was valid
     * @param xmlConverters List of converters needed to convert the security.xml of this version to the next
     *                      one
     */
    SecurityVersion(ArtifactoryVersion from, ArtifactoryVersion until, XmlConverter... xmlConverters) {
        this.comparator = new VersionComparator(from, until);
        this.xmlConverters = xmlConverters;
    }

    public boolean isCurrent() {
        return comparator.isCurrent();
    }

    @Override
    public VersionComparator getComparator() {
        return comparator;
    }

    public XmlConverter[] getXmlConverters() {
        return xmlConverters;
    }

    public String convert(String securityXmlAsString) {
        // First create the list of converters to apply
        List<XmlConverter> converters = new ArrayList<>();

        // All converters of versions above me needs to be executed in sequence
        SecurityVersion[] versions = SecurityVersion.values();
        for (SecurityVersion version : versions) {
            if (version.ordinal() >= ordinal() && version.getXmlConverters() != null) {
                converters.addAll(Arrays.asList(version.getXmlConverters()));
            }
        }

        return XmlConverterUtils.convert(converters, securityXmlAsString);
    }

    public static SecurityVersion getCurrent() {
        SecurityVersion[] versions = SecurityVersion.values();
        for (SecurityVersion version : versions) {
            if (version.isCurrent()) {
                return version;
            }
        }
        throw new IllegalStateException("Should have a current version!");
    }

    public static SecurityVersion findVersion(String securityData) {
        // Version exists since v4
        int versionIdx = securityData.indexOf(VERSION_ATT);
        if (versionIdx != -1) {
            int versionStartIndex = versionIdx + VERSION_ATT.length();
            String cutVersion = securityData.substring(versionStartIndex);
            String isolatedVersion = cutVersion.substring(0, cutVersion.indexOf('"'));
            return valueOf(isolatedVersion);
        } else {
            // Hack to find old versions
            int groupsIdx = securityData.indexOf("<groups>");
            int userIdx = securityData.indexOf("<user>");
            int simpleUserIdx = securityData.indexOf("SimpleUser>");
            int aclIdIdx = securityData.indexOf("<aclObjectIdentity");
            if (userIdx != -1 || groupsIdx != -1) {
                return v3;
            }
            if (aclIdIdx != -1) {
                return v1;
            }
            if (simpleUserIdx != -1) {
                return v2;
            }
            return unsupported;
        }
    }
}
