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

package org.artifactory.repo.service.mover;

import org.artifactory.api.util.Builder;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;

/**
 * Repo path mover configuration object builder
 *
 * @author Noam Y. Tenne
 */
public class MoverConfigBuilder implements Builder<MoverConfig> {
    private RepoPath fromRepoPath;
    private RepoPath targetLocalRepoPath;
    private boolean copy = false;
    private boolean dryRun = false;
    private boolean executeMavenMetadataCalculation = false;
    private boolean pruneEmptyFolders = false;
    private Properties properties;
    private boolean suppressLayouts = false;
    private boolean failFast = false;
    private boolean unixStyleBehavior = true;

    public MoverConfigBuilder(RepoPath fromRepoPath, RepoPath targetLocalRepoPath) {
        this.fromRepoPath = fromRepoPath;
        this.targetLocalRepoPath = targetLocalRepoPath;
    }

    /**
     * Indicate if a copy is being performed
     *
     * @param copy True if performing a copy, false if not
     * @return MoverConfigBuilder
     */
    public MoverConfigBuilder copy(boolean copy) {
        this.copy = copy;
        return this;
    }

    /**
     * Indicate if the current run is a dry one (no items actually moved)
     *
     * @param run True if run is dry, false if not
     * @return MoverConfigBuilder
     */
    public MoverConfigBuilder dryRun(boolean run) {
        this.dryRun = run;
        return this;
    }

    /**
     * Indicates if Unix-style behavior should be used when dealing with existing nested folders
     * @see org.artifactory.repo.service.mover.MoverConfig
     *
     * @param unixStyleBehavior True if Unix-style behavior should be used
     * @return MoverConfigBuilder
     */
    public MoverConfigBuilder unixStyleBehavior(boolean unixStyleBehavior) {
        this.unixStyleBehavior = unixStyleBehavior;
        return this;
    }

    /**
     * @param properties
     * @return MoverConfigBuilder
     */
    public MoverConfigBuilder properties(Properties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Indicate if the metadata should be calculated immediately or scheduled
     *
     * @param calculation True if metadata should be calculated immediately, false if not
     * @return MoverConfigBuilder
     */
    public MoverConfigBuilder executeMavenMetadataCalculation(boolean calculation) {
        this.executeMavenMetadataCalculation = calculation;
        return this;
    }

    /**
     * Indicate if search results are being moved (will perform empty dir cleanup)
     *
     * @param pruneEmptyFolders True if should prune empty folders after move (usually when moving search results or during a build promotion)
     * @return MoverConfigBuilder
     */
    public MoverConfigBuilder pruneEmptyFolders(boolean pruneEmptyFolders) {
        this.pruneEmptyFolders = pruneEmptyFolders;
        return this;
    }

    /**
     * Indicate whether path translation across different layouts should be suppressed.
     *
     * @param suppressLayouts True if path translation should be suppressed
     * @return MoverConfigBuilder
     */
    public MoverConfigBuilder suppressLayouts(boolean suppressLayouts) {
        this.suppressLayouts = suppressLayouts;
        return this;
    }

    /**
     * Indicate whether the operation should fail upon encountering an error.
     *
     * @param failFast True if the operation should fail upon encountering an error.
     * @return MoverConfigBuilder
     */
    public MoverConfigBuilder failFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    /**
     * Builds a mover configuration object with the given parameters
     *
     * @return MoverConfig
     */
    @Override
    public MoverConfig build() {
        return new MoverConfig(fromRepoPath, targetLocalRepoPath, copy, dryRun,executeMavenMetadataCalculation,
                pruneEmptyFolders, properties, suppressLayouts, failFast, unixStyleBehavior);
    }
}