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

package org.artifactory.addon;

/**
 * @author Yoav Aharoni
 */
public enum AddonType {
    //
    //PLEASE MAKE SURE THESE DETAILS ARE CONSISTENT WITH THE ONES IN THE PROPERTY FILES
    //
    AOL("aol", "Artifactory Online", -1, new String[]{"all"}, "pro", "aol"),
    BUILD("build", "Build Integration", 100, new String[]{"all", "features"}, "pro", "Build+Integration"),
    MULTIPUSH("multipush", "Multipush Replication", 100, new String[]{"all", "enterprise"}, "ent",
            "Repository+Replication#RepositoryReplication-Multi-pushReplication"),
    LICENSES("license", "License Control", 200, new String[]{"all", "features"}, "pro", "License+Control"),
    REST("rest", "Advanced REST", 300, new String[]{"all", "features"}, "pro", "Artifactory+REST+API"),
    LDAP("ldap", "LDAP Groups", 400, new String[]{"all", "features"}, "pro", "Ldap+Groups"),
    REPLICATION("replication", "Repository Replication", 500, new String[]{"all", "features"}, "pro",
            "Repository+Replication"),
    PROPERTIES("properties", "Properties", 600, new String[]{"all", "features"}, "pro", "Properties"),
    SEARCH("search", "Smart Searches", 700, new String[]{"all", "features"}, "pro", "Smart+Searches"),
    PLUGINS("plugins", "User Plugins", 800, new String[]{"all", "features"}, "pro", "User+Plugins"),
    YUM("yum", "YUM", 900, new String[]{"all", "packageManagement"}, "pro", "YUM+Repositories"),
    P2("p2", "P2", 1000, new String[]{"all", "packageManagement"}, "pro", "P2+Repositories"),
    NUGET("nuget", "NuGet", 1100, new String[]{"all", "packageManagement"}, "pro", "Nuget+Repositories"),
    LAYOUTS("layouts", "Repository Layouts", 1200, new String[]{"all", "features"}, "pro", "Repository+Layouts"),
    FILTERED_RESOURCES("filtered-resources", "Filtered Resources", 1300, new String[]{"all", "features"}, "pro",
            "Filtered+Resources"),
    SSO("sso", "Crowd & SSO", 1400, new String[]{"all"}, "pro", "Atlassian+Crowd+Integration"),
    WATCH("watch", "Watches", 1500, new String[]{"all", "features"}, "pro", "Watches"),
    WEBSTART("webstart", "Jar Signing", 1600, new String[]{"all", "features"}, "pro",
            "WebStart+and+Jar+Signing"),
    BLACKDUCK("blackduck", "Black Duck Integration", 250, new String[]{"all"}, "pro",
            "Black+Duck+Code+Center+Integration"),
    GEMS("gems", "RubyGems", 1100, new String[]{"all", "packageManagement"}, "pro", "RubyGems+Repositories"),
    NPM("npm", "npm", 860, new String[]{"all", "packageManagement"}, "pro", "NPM+Repositories"),
    BOWER("bower", "Bower", 870, new String[]{"all", "packageManagement"}, "pro", "Bower+Repositories"),
    DEBIAN("debian", "Debian", 900, new String[]{"all", "packageManagement"}, "pro", "Debian+Repositories"),
    PYPI("pypi", "PyPI", 970, new String[]{"all", "packageManagement"}, "pro", "PyPI+Repositories"),
    DOCKER("docker", "Docker", 910, new String[]{"all", "packageManagement"}, "pro", "Docker+Repositories"),
    VAGRANT("vagrant", "Vagrant", 915, new String[]{"all", "packageManagement"}, "pro", "Vagrant+Repositories"),
    VCS("vcs", "VCS", 920, new String[]{"all", "packageManagement"}, "pro", "VCS+Repositories"),
    GITLFS("git-lfs", "Git LFS", 930, new String[]{"all", "packageManagement"}, "pro", "Git+LFS+Repositories"),
    HA("ha", "High Availability", 2000, new String[]{"all", "enterprise"}, "ent", "Artifactory+High+Availability"),
    FILE_STORE("fileStore", "S3 Object Store", 2000, new String[]{"all", "enterprise"}, "ent", "S3+Object+Storage"),
    AQL("aql", "AQL", 2000, new String[]{"all", "features"}, "oss",
            "Artifactory+Query+Language"),
    MAVEN_PLUGIN("mavenPlugin", "Maven Plugin", 2000, new String[]{"all", "ecosystem"}, "oss",
            "Maven+Artifactory+Plugin"),
    GRADLE_PLUGIN("gradlePlugin", "Gradle Plugin", 2000, new String[]{"all", "ecosystem"}, "oss",
            "Gradle+Artifactory+Plugin"),
    JENKINS_PLUGIN("jenkinsPlugin", "Jenkins Plugin", 2000, new String[]{"all", "ecosystem"}, "oss",
            "Jenkins+(Hudson)+Artifactory+Plug-in"),
    BAMBOO_PLUGIN("bambooPlugin", "Bamboo Plugin", 2000, new String[]{"all", "ecosystem"}, "oss",
            "Bamboo+Artifactory+Plug-in"),
    TC_PLUGIN("tcPlugin", "TeamCity Plugin", 2000, new String[]{"all", "ecosystem"}, "oss",
            "TeamCity+Artifactory+Plug-in"),
    MSBUILD_PLUGIN("msbuildPlugin", "MSBuild/TFS Plugin", 2000, new String[]{"all", "ecosystem"}, "oss",
            "MSBuild+Artifactory+Plugin"),
    BINTRAY_INTEGRATION("bintrayIntegration", "Bintray Integration", 2000, new String[]{"all", "ecosystem"}, "oss",
            "Bintray+Integration");

    private String addonName;
    private String addonDisplayName;
    private int displayOrdinal;
    private String[] categories;
    private String type;
    private String configureUrlSuffix;

    AddonType(String addonName, String addonDisplayName, int displayOrdinal, String[] categories, String type, String configureUrlSuffix) {
        this.addonName = addonName;
        this.addonDisplayName = addonDisplayName;
        this.displayOrdinal = displayOrdinal;
        this.categories = categories;
        this.type = type;
        this.configureUrlSuffix = configureUrlSuffix;
    }

    public String getAddonDisplayName() {
        return addonDisplayName;
    }

    public String getAddonName() {
        return addonName;
    }

    public int getDisplayOrdinal() {
        return displayOrdinal;
    }

    public String[] getCategories() {
        return categories;
    }

    public String getType() {
        return type;
    }

    public String getConfigureUrlSuffix() {
        return configureUrlSuffix;
    }
}