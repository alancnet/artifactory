/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.addon.watch;

import org.artifactory.addon.Addon;
import org.artifactory.fs.WatchersInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.Pair;

import java.util.Map;

/**
 * @author mamo
 */
public interface ArtifactWatchAddon extends Addon {

    Map<RepoPath, WatchersInfo> getAllWatchers(RepoPath repoPath);

    /**
     * remove watcher by name and repo path
     *
     * @param repoPath  - artifact repo path
     * @param watchUser - watcher name
     */
    void removeWatcher(RepoPath repoPath,String watchUser);

    /**
     * add watcher by name and repo path
     *
     * @param repoPath  - artifact repo path
     * @param watchUser - watcher name
     */
    void addWatcher(RepoPath repoPath, String watcherUsername);


    /**
     * check if user is currently watching this repo path
     * @param repoPath - repo path
     * @param userName - user Name
     * @return if true , user is watching
     */
     boolean isUserWatchingRepo(RepoPath repoPath,String userName);


    Pair<RepoPath, WatchersInfo> getNearestWatchDefinition(RepoPath repoPath, String userName);


    /**
     * get watches for repo path
     *
     * @param repoPath - repo path
     * @return watches for repo path
     */
    WatchersInfo getWatchers(RepoPath repoPath);

    }
