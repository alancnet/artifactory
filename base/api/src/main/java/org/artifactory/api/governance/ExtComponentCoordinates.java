package org.artifactory.api.governance;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.module.ModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * @author Dan Feldman
 * @author Chen Keinan
 */
public class ExtComponentCoordinates implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(ExtComponentCoordinates.class);

    //Packages that are separated by '/' and have a version that consists of 4 numbers are converted to links by browsers
    private static final String MAIL_COMPATIBLE_DELIMITER = ":";

    //Namespace prefixes
    public enum PackageType {
        MAVEN("org.apache.maven"), NUGET("org.nuget"), UNSPECIFIED("");

        private final String stringValue;

        PackageType(final String s) {
            stringValue = s;
        }

        public String toString() {
            return stringValue;
        }
    }

    private PackageType packageType;
    private String coordinates;
    private String groupId;
    private String name;
    private String nameSpace;
    private String version;
    private String externalId;
    private String mailCompatibleCoordinates; // :(

    public ExtComponentCoordinates() {

    }

    /**
     * To be used only when coordinates are not used to directly resolve components (i.e.
     */
    public ExtComponentCoordinates(String coordinates) {
        name = "";
        version = "";
        packageType = PackageType.UNSPECIFIED;
        this.coordinates = coordinates;
    }

    /**
     * No groupId (i.e. NuGet)
     */
    public ExtComponentCoordinates(String packageName, String packageVersion, PackageType packageType) {
        this.name = StringUtils.isNotBlank(packageName) ? packageName : "";
        this.version = StringUtils.isNotBlank(packageVersion) ? packageVersion : "";
        this.packageType = packageType;
        this.coordinates = assembleCoordinates(packageType, null, packageName, packageVersion);
    }

    /**
     * Maven
     */
    public ExtComponentCoordinates(MavenArtifactInfo artifactInfo) {
        this.name = StringUtils.isNotBlank(artifactInfo.getArtifactId()) ? artifactInfo.getArtifactId() : "";
        this.version = StringUtils.isNotBlank(artifactInfo.getVersion()) ? artifactInfo.getVersion() : "";
        this.groupId = StringUtils.isNotBlank(artifactInfo.getGroupId()) ? artifactInfo.getGroupId() : "";
        this.packageType = PackageType.MAVEN;
        this.coordinates = assembleCoordinates(packageType, groupId, name, version);
    }


    public ExtComponentCoordinates(ModuleInfo moduleInfo, PackageType packageType) {
        this.groupId = StringUtils.isNotBlank(moduleInfo.getOrganization()) ? moduleInfo.getOrganization() : "";
        this.name = StringUtils.isNotBlank(moduleInfo.getModule()) ? moduleInfo.getModule() : "";
        StringBuilder version = new StringBuilder(StringUtils.isNotBlank(moduleInfo.getBaseRevision())
                ? moduleInfo.getBaseRevision() : "");
        version.append(StringUtils.isNotBlank(moduleInfo.getFolderIntegrationRevision())
                ? "-" + moduleInfo.getFolderIntegrationRevision() : "");
        this.version = version.toString();
        this.packageType = packageType;
        this.coordinates = assembleCoordinates(packageType, groupId, name, this.version);
    }

    private String assembleCoordinates(PackageType packageType, String groupId, String name, String version) {
        String delimiter = getDelimiterByPackageType(packageType);
        StringBuilder coordinates = new StringBuilder();
        StringBuilder mailCoordinates = new StringBuilder();
        if (StringUtils.isNotBlank(groupId)) {
            coordinates.append(groupId).append(delimiter);
            mailCoordinates.append(groupId).append(MAIL_COMPATIBLE_DELIMITER);
        }
        if (StringUtils.isNotBlank(name)) {
            coordinates.append(name);
            mailCoordinates.append(name);
        }
        if (StringUtils.isNotBlank(version)) {
            coordinates.append(delimiter).append(version);
            mailCoordinates.append(MAIL_COMPATIBLE_DELIMITER).append(version);
        }
        log.debug("Creating Black Duck component coordinates for {} package: {}, version: {}, with Code Center " +
                        "Namespace: {}.  Coordinates are: {}",
                StringUtils.isBlank(groupId) ? "" : "groupId: " + groupId,
                name, version, packageType, coordinates);
        this.mailCompatibleCoordinates = mailCoordinates.toString();
        return coordinates.toString();
    }

    public String getNamespace() {
        return this.nameSpace;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public String getMailCompatibleCoordinates() {
        return mailCompatibleCoordinates;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    /**
     * @return externalId if set, coordinates if not
     */
    public String getExternalId() {
        return StringUtils.isNotBlank(externalId) ? externalId : coordinates;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(name) && StringUtils.isNotBlank(version) && StringUtils.isNotBlank(coordinates)
                && packageType != null;
    }

    private String getDelimiterByPackageType(PackageType packageType) {
        if (PackageType.MAVEN.equals(packageType)) {
            return ":";
        } else if (PackageType.NUGET.equals(packageType)) {
            return "/";
        }
        return "";
    }

    public PackageType getPackageType() {
        return packageType;
    }

    public void setPackageType(PackageType packageType) {
        this.packageType = packageType;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setMailCompatibleCoordinates(String mailCompatibleCoordinates) {
        this.mailCompatibleCoordinates = mailCompatibleCoordinates;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }
}
