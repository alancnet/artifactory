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

package org.artifactory.model.xstream.fs;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.artifactory.fs.MutableWatcherInfo;
import org.artifactory.fs.WatcherInfo;

import java.util.Date;

/**
 * Item watcher metadata object
 *
 * @author Noam Tenne
 */
@XStreamAlias(WatcherInfo.ROOT)
public class WatcherImpl implements Comparable, MutableWatcherInfo {

    private String username;
    private long watchingSinceTime;

    /**
     * Default constructor
     *
     * @param username          Username of watcher
     * @param watchingSinceTime The time the user has begun watching the item
     */
    public WatcherImpl(String username, long watchingSinceTime) {
        this.username = username;
        this.watchingSinceTime = watchingSinceTime;
    }

    /**
     * Copy constructor
     *
     * @param watcher Watcher object to copy
     */
    public WatcherImpl(WatcherInfo watcher) {
        username = watcher.getUsername();
        watchingSinceTime = watcher.getWatchingSinceTime();
    }

    /**
     * Returns the user name
     *
     * @return Username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username
     *
     * @param username Username
     */
    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the time the user has began watching the item
     *
     * @return Watching since time
     */
    @Override
    public long getWatchingSinceTime() {
        return watchingSinceTime;
    }

    /**
     * Returns the date the user has began watching the item
     *
     * @return Watching since date
     */
    @Override
    public Date getWatchingSinceDate() {
        return new Date(watchingSinceTime);
    }

    /**
     * Sets the time the user has began watching the item
     *
     * @param watchingSinceTime Watching since time
     */
    @Override
    public void setWatchingSinceTime(long watchingSinceTime) {
        this.watchingSinceTime = watchingSinceTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WatcherInfo)) {
            return false;
        }

        WatcherImpl watcher = (WatcherImpl) o;

        if (watchingSinceTime != watcher.watchingSinceTime) {
            return false;
        }
        if (username != null ? !username.equals(watcher.username) : watcher.username != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (int) (watchingSinceTime ^ (watchingSinceTime >>> 32));
        return result;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof WatcherInfo) {
            WatcherImpl other = (WatcherImpl) o;
            return (watchingSinceTime < other.watchingSinceTime ? -1 :
                    (watchingSinceTime == other.watchingSinceTime ? 0 : 1));
        }
        return 0;
    }
}
