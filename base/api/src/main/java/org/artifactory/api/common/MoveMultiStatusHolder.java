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

package org.artifactory.api.common;

import com.google.common.collect.Sets;
import org.artifactory.repo.RepoPath;

import java.util.Set;

/**
 * A custom multi status holder for the different artifact move actions, which retains a counter that represents the
 * number of artifacts that have been moved in the move operation that was invoked and associated with this holder
 *
 * @author Noam Tenne
 */
public class MoveMultiStatusHolder extends BasicStatusHolder {

    /**
     * Represents the number of artifacts that have been moved
     */
    private int artifactsMovedCounter = 0;


    /**
     * Represents the number of folders that have been moved
     */
    private int foldersMovedCounter = 0;

    /**
     * Folders repo paths to calculate maven metadata on when the entire move/copy finishes
     */
    private Set<RepoPath> candidatesForMavenMetadataCalculation = Sets.newHashSet();

    /**
     * Raises the moved artifacts counter by 1
     */
    public void artifactMoved() {
        artifactsMovedCounter++;
    }

    /**
     * Raises the moved folder counter by 1
     */
    public void folderMoved() {
        foldersMovedCounter++;
    }

    /**
     * Add folder path to calculate maven metadata when the move/copy process finishes
     *
     * @param repoPath The folder repo path, will be calculated non-recursively later on
     */
    public void addToMavenMetadataCandidates(RepoPath repoPath) {
        candidatesForMavenMetadataCalculation.add(repoPath);
    }

    /**
     * Returns the total number of artifacts that have been moved
     *
     * @return Total artifacts moved
     */
    public int getMovedArtifactsCount() {
        return artifactsMovedCounter;
    }

    /**
     * Returns the total number of folders that have been moved
     *
     * @return Total folders moved
     */
    public int getMovedFoldersCount() {
        return foldersMovedCounter;
    }

    public Set<RepoPath> getCandidatesForMavenMetadataCalculation() {
        return candidatesForMavenMetadataCalculation;
    }

    @Override
    public void merge(BasicStatusHolder toMerge) {
        super.merge(toMerge);
        if (toMerge instanceof MoveMultiStatusHolder) {
            //Merge moved items counter
            MoveMultiStatusHolder moveMultiStatusHolder = (MoveMultiStatusHolder) toMerge;
            artifactsMovedCounter += moveMultiStatusHolder.getMovedArtifactsCount();
            foldersMovedCounter += moveMultiStatusHolder.getMovedFoldersCount();
            candidatesForMavenMetadataCalculation.addAll(
                    ((MoveMultiStatusHolder) toMerge).getCandidatesForMavenMetadataCalculation());
        }
    }
}
