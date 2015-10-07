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

package org.artifactory.api.repo;

import org.artifactory.repo.RepoPath;
import org.artifactory.repo.remote.browse.RemoteItem;

/**
 * Represents an item on a remote server.
 *
 * @author Tomer Cohen
 */
public class RemoteBrowsableItem extends BrowsableItem {

    /**
     * The effective url for linking this item
     */
    private String effectiveUrl;

    /**
     * Creates a new remote browsable item.
     *
     * @param remoteItem Remote item details
     * @param repoPath   Item repo path
     */
    public RemoteBrowsableItem(RemoteItem remoteItem, RepoPath repoPath) {
        super(remoteItem.getName(), remoteItem.isDirectory(), 0L,
                remoteItem.getLastModified(), remoteItem.getSize(), repoPath);
        setRemote(true);
    }

    public String getEffectiveUrl() {
        return effectiveUrl;
    }

    public void setEffectiveUrl(String effectiveUrl) {
        this.effectiveUrl = effectiveUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RemoteBrowsableItem)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        RemoteBrowsableItem item = (RemoteBrowsableItem) o;

        if (name != null ? !name.equals(item.name) : item.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
