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

package org.artifactory.sapi.fs;

import org.artifactory.fs.MutableItemInfo;
import org.artifactory.fs.WatcherInfo;
import org.artifactory.md.Properties;

/**
 * The mutable additions for a virtual item.
 *
 * @author Yossi Shaul
 */
public interface MutableVfsItem<T extends MutableItemInfo> extends VfsItem<T> {

    /**
     * @return True if this item is new and doesn't exist in the database yet
     */
    boolean isNew();

    /**
     * Marks this for deletion on session save. After the session is saved all the deleted items are automatically
     * unlocked and removed from the session.
     *
     * @return TODO: no need to return a value?
     */
    boolean delete();

    /**
     * @return True is this item was deleted from the database. This only happens after the item is marked for
     * deletion and the session is saved.
     */
    boolean isDeleted();

    boolean hasPendingChanges();

    boolean isMarkedForDeletion();

    void save();

    void setCreated(long created);

    void setCreatedBy(String createBy);

    void setModified(long modified);

    void setModifiedBy(String modifiedBy);

    void setUpdated(long updated);

    void setProperties(Properties properties);

    void addWatch(WatcherInfo watch);

    /**
     * Marks the state of this item as in error. Items in error state should not be saved by the session manager.
     * This should not be used occasionally, only in cases where on one hand we don't want to split a transaction to
     * multiple small ones and on the other hand we don't want single failure to rollback the entire transaction.
     */
    void markError();

    /**
     * Releases any resources held by the current mutable item.
     * This is called just before the write lock of the mutable item is released, regardless of the item state (success or failure).
     */
    void releaseResources();
}
