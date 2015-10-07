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

import java.io.Serializable;

/**
 * Base class of simple browsable item
 *
 * @author Noam Y. Tenne
 */
public abstract class BaseBrowsableItem implements Serializable, Comparable<BaseBrowsableItem> {

    private static final long serialVersionUID = 1L;

    public static final String UP = "..";
    protected String name;
    private boolean folder;
    private long created;
    private long lastModified;
    private long size;
    private boolean remote = false;


    /**
     * Main constructor
     *
     * @param name         Item display name
     * @param folder       True if the item represents a folder
     * @param created
     * @param lastModified Item last modified time
     * @param size         Item size (applicable only to files)
     */
    public BaseBrowsableItem(String name, boolean folder, long created, long lastModified, long size) {
        this.name = name;
        this.folder = folder;
        this.created = created;
        this.lastModified = lastModified;
        this.size = size;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    /**
     * Returns the display name of the item
     *
     * @return Item display name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the repo path of the item
     *
     * @return Item repo path
     */
    public abstract RepoPath getRepoPath();

    /**
     * Returns the repo key of the item
     *
     * @return Item repo key
     */
    public abstract String getRepoKey();

    /**
     * Returns the relative path of the item
     *
     * @return Item relative path
     */
    public abstract String getRelativePath();

    /**
     * Returns the last modified time of the item
     *
     * @return Item last modified time in millis
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * Returns the creation time of the item
     *
     * @return Item creation time in millis
     */
    public long getCreated() {
        return created;
    }

    /**
     * Returns the size of the item
     *
     * @return Item size
     */
    public long getSize() {
        return size;
    }

    public boolean isFolder() {
        return folder;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return getRepoPath().toString();
    }
}
