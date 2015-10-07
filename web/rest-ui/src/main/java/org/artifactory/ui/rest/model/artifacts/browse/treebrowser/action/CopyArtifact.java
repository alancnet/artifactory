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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action;


/**
 * @author Chen Keinan
 */
public class CopyArtifact extends BaseArtifact {

    private String targetRepoKey;
    private String targetPath;
    private boolean dryRun;
    private boolean suppressLayouts;
    private boolean failFast;

    public CopyArtifact() {
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public boolean isSuppressLayouts() {
        return suppressLayouts;
    }

    public void setSuppressLayouts(boolean suppressLayouts) {
        this.suppressLayouts = suppressLayouts;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public String getTargetRepoKey() {
        return targetRepoKey;
    }

    public void setTargetRepoKey(String targetRepoKey) {
        this.targetRepoKey = targetRepoKey;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public CopyArtifact(String name) {
        super(name);
    }
}
