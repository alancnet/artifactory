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

package org.artifactory.addon.p2;

import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;

/**
 * Model of a local or remote repository for the UI
 *
 * @author Shay Yaakov
 */
public class P2Repo implements Serializable {

    private String repoKey;

    private String repoUrl;

    // TODO: [by dan] if this doesn't work when this object is nested in P2TypeSpecific need to duplicate model in rest-ui module
    @JsonIgnore
    private RepoDescriptor descriptor;

    public P2Repo() {

    }

    public P2Repo(RepoDescriptor descriptor, String repoKey, String repoUrl) {
        this.descriptor = descriptor;
        this.repoKey = repoKey;
        this.repoUrl = repoUrl;
    }

    @JsonIgnore
    public boolean isRemote() {
        return descriptor instanceof RemoteRepoDescriptor;
    }

    @JsonIgnore
    public RepoDescriptor getDescriptor() {
        return descriptor;
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }
}
