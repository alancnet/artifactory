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

package org.artifactory.api.rest.build.artifacts;

import org.artifactory.api.archive.ArchiveType;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * A request wrapper object which holds all the necessary parameters
 * for the build artifacts search and archive REST APIs.
 *
 * @author Shay Yaakov
 */
@XmlRootElement
public class BuildArtifactsRequest implements Serializable {

    /**
     * The build name to get artifacts from
     */
    private String buildName;

    /**
     * The build number to get artifacts from, can be the const LATEST for latest build
     */
    private String buildNumber;

    /**
     * The build status to get artifacts from, will return the last build with this status
     */
    private String buildStatus;

    /**
     * limit the search to specific repos, if null then get from all real repos
     */
    private List<String> repos;

    /**
     * Optionally list of mappings between input regexp to search artifacts with and output regexp to lay out the found artifacts,
     * (Usually the output pattern is used for build artifacts archive resource and represents the final archive layout).
     */
    private List<BuildArtifactsMapping> mappings;

    /**
     * The archive type to send back to the user (valid only for build artifacts archive resource), can be zip/tar/tar.gz/tgz
     */
    private ArchiveType archiveType;

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getBuildStatus() {
        return buildStatus;
    }

    public void setBuildStatus(String buildStatus) {
        this.buildStatus = buildStatus;
    }

    public List<String> getRepos() {
        return repos;
    }

    public void setRepos(List<String> repos) {
        this.repos = repos;
    }

    public List<BuildArtifactsMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<BuildArtifactsMapping> mappings) {
        this.mappings = mappings;
    }

    public String getArchiveType() {
        if (archiveType == null) {
            return null;
        }
        return archiveType.value();
    }

    @JsonIgnore
    public ArchiveType getType() {
        return archiveType;
    }

    public void setArchiveType(String archiveTypeValue) {
        this.archiveType = ArchiveType.fromValue(archiveTypeValue);
    }
}
