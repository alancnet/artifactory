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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action;

import org.artifactory.api.module.VersionUnit;
import org.artifactory.mime.MavenNaming;
import org.artifactory.ui.rest.model.common.RepoKeyPath;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Chen Keinan
 */
public class DeleteArtifactVersion extends BaseArtifact {
    private String groupId;
    private String version;
    private Integer directoriesCount;
    private String folderIntegrationRevision = "";
    private Set<RepoKeyPath> repoPaths = new HashSet<>();

    DeleteArtifactVersion() {

    }

    public DeleteArtifactVersion(String name) {
        super(name);
    }

    public DeleteArtifactVersion(VersionUnit result) {
        groupId = result.getModuleInfo().getOrganization();
        version = buildVersion(result);
        result.getRepoPaths().forEach(repoPath ->
                repoPaths.add(new RepoKeyPath(repoPath.getPath(), repoPath.getRepoKey())));
    }

    /**
     * build version full name
     *
     * @param result - version unit result
     * @return full version name
     */
    private String buildVersion(VersionUnit result) {
        StringBuilder groupVersionKeyBuilder = new StringBuilder(result.getModuleInfo().getBaseRevision());
        if (result.getModuleInfo().isIntegration()) {
            groupVersionKeyBuilder.append("-");
            if (MavenNaming.SNAPSHOT.equals(result.getModuleInfo().getFolderIntegrationRevision())) {
                groupVersionKeyBuilder.append(MavenNaming.SNAPSHOT);
            } else {
                groupVersionKeyBuilder.append("INTEGRATION");
            }
        }
        return groupVersionKeyBuilder.toString();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getVersion() {
        return version + folderIntegrationRevision;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getDirectoriesCount() {
        return directoriesCount;
    }

    public void setDirectoriesCount(Integer directoriesCount) {
        this.directoriesCount = directoriesCount;
    }

    public Set<RepoKeyPath> getRepoPaths() {
        return repoPaths;
    }

    public void setRepoPaths(Set<RepoKeyPath> repoPaths) {
        this.repoPaths = repoPaths;
    }
}