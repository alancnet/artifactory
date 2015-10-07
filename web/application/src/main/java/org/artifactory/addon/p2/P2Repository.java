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

import java.io.Serializable;

/**
 * Model of a local or remote repository for the UI
 *
 * @author Shay Yaakov
 */
// TODO: [by dan] delete this when wicket dies
public class P2Repository implements Serializable {

    private String repoKey;

    private String repoUrl;

    /**
     * Repo is already a part of the virtual repo list
     */
    private boolean alreadyIncluded;

    /**
     * Repo is already defined as a repository
     */
    private boolean exists;
    /**
     * Repo exists but will be modified to enable p2 upon save
     */
    private boolean modified;
    /**
     * New remote repo will be created
     */
    private boolean toCreate;
    /**
     * True if the user selected the checkbox to approve the action on this remote repo
     */
    private boolean selected;

    private RepoDescriptor descriptor;

    public P2Repository(RepoDescriptor descriptor, String repoKey, String repoUrl,
            boolean alreadyIncluded,
            boolean exists,
            boolean modified,
            boolean toCreate) {
        this.descriptor = descriptor;
        this.repoKey = repoKey;
        this.repoUrl = repoUrl;
        this.alreadyIncluded = alreadyIncluded;
        this.exists = exists;
        this.modified = modified;
        this.toCreate = toCreate;
        this.selected = true;
    }

    public boolean isRemote() {
        return descriptor instanceof RemoteRepoDescriptor;
    }

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

    public boolean isAlreadyIncluded() {
        return alreadyIncluded;
    }

    public void setAlreadyIncluded(boolean alreadyIncluded) {
        this.alreadyIncluded = alreadyIncluded;
    }

    public boolean isExists() {
        return exists;
    }

    public boolean isModified() {
        return modified;
    }

    public boolean isToCreate() {
        return toCreate;
    }

    public boolean isSelected() {
        return selected || alreadyIncluded;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
