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

import java.io.Serializable;

/**
 * The model class for P2 remote repositories to create/modify/add.
 *
 * @author Yossi Shaul
 */
// TODO: [by dan] kill this when wicket dies
public class P2RepositoryModel implements Serializable {

    private P2Repository p2Repository;

    /**
     * True if the checkbox can be checked. It cannot when there is nothing to do (already included).
     */
    boolean selectable;

    public P2RepositoryModel(P2Repository p2Repository) {
        this.p2Repository = p2Repository;
        // If already included and supports P2 (no need to modify), checkbox is disabled
        selectable = !p2Repository.isAlreadyIncluded() || p2Repository.isModified();
    }

    public P2Repository getP2Repository() {
        return p2Repository;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public boolean isSelected() {
        return p2Repository.isSelected();
    }

    /**
     * Toggle selection of the checkbox. Called from the UI.
     */
    public void setSelected(boolean selected) {
        p2Repository.setSelected(selected);
    }

    public boolean isToCreate() {
        return p2Repository.isToCreate();
    }

    public String getRepoKey() {
        return p2Repository.getRepoKey();
    }

    /**
     * Sets a new repo key. Called from the UI.
     */
    public void setRepoKey(String repoKey) {
        p2Repository.setRepoKey(repoKey);
    }

    public String getRepoUrl() {
        return p2Repository.getRepoUrl();
    }

    public String getAction() {
        if (p2Repository.isToCreate()) {
            return "Create";
        } else if (p2Repository.isModified()) {
            return "Modify";
        } else if (p2Repository.isExists() && !p2Repository.isAlreadyIncluded()) {
            return "Include";
        } else { // already included
            return "Included";
        }
    }
}
