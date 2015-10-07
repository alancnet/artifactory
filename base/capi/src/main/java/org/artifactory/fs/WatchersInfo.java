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

package org.artifactory.fs;

import org.artifactory.common.Info;

import java.util.Set;

/**
 * Date: 8/3/11
 * Time: 2:23 PM
 *
 * @author Fred Simon
 */
public interface WatchersInfo extends Info {
    String ROOT = "watchers";

    Set<WatcherInfo> getWatchers();

    WatcherInfo getWatcher(String username);

    boolean isUserWatching(String username);

    boolean isEmpty();
}
