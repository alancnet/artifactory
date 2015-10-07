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

package org.artifactory.update.utils;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionReader;

import java.io.File;

/**
 * @author freds
 * @date Nov 9, 2008
 */
public abstract class BackupUtils {
    private BackupUtils() {
        // utility class
    }

    public static ArtifactoryVersion findVersion(File backupFolder) {
        if (backupFolder == null || !backupFolder.exists()) {
            throw new IllegalArgumentException("Cannot find Artifactory of null or non existent folder");
        }

        File propFile = new File(backupFolder, ArtifactoryHome.ARTIFACTORY_PROPERTIES_FILE);
        if (!propFile.exists()) {
            throw new IllegalArgumentException("Backup folder " + backupFolder.getAbsolutePath() +
                    " does not contain file :" + propFile.getName());
        }

        return ArtifactoryVersionReader.read(propFile).getVersion();
    }
}
