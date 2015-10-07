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

import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.rest.common.util.JsonUtil;

import java.util.Collection;
import java.util.List;

/**
 * Holder for multiple {@link DeleteArtifactVersion}, needed for sending back warnings about the search operation.
 *
 * @author Dan Feldman
 */
public class DeleteArtifactVersions extends BaseModel {

    Collection<DeleteArtifactVersion> versions;

    public DeleteArtifactVersions() {

    }

    public DeleteArtifactVersions(Collection<DeleteArtifactVersion> versions) {
        this.versions = versions;
    }

    public Collection<DeleteArtifactVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<DeleteArtifactVersion> versions) {
        this.versions = versions;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}