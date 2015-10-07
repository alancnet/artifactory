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

package org.artifactory.version;

import org.apache.commons.io.IOUtils;
import org.artifactory.common.ConstantValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Returns ArtifactoryVersion object from a properties stream/file.
 *
 * @author Yossi Shaul
 */
public class ArtifactoryVersionReader {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryVersionReader.class);

    public static CompoundVersionDetails read(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Artifactory properties input stream cannot be null");
        }
        Properties props = new Properties();
        try {
            props.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read input property stream", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        String versionString = props.getProperty(ConstantValues.artifactoryVersion.getPropertyName());
        String revisionString = props.getProperty(ConstantValues.artifactoryRevision.getPropertyName());
        String timestampString = props.getProperty(ConstantValues.artifactoryTimestamp.getPropertyName());

        return getCompoundVersionDetails(versionString, revisionString, timestampString);
    }

    public static CompoundVersionDetails getCompoundVersionDetails(String versionString, String revisionString,
            String timestampString) {
        ArtifactoryVersion matchedVersion = null;
        // If current version or development version=${project.version.prop} or revision=${buildNumber}
        if (ArtifactoryVersion.getCurrent().getValue().equals(versionString) ||
                versionString.startsWith("${") ||
                versionString.endsWith("-SNAPSHOT") ||
                revisionString.startsWith("${")) {
            // Just return the current version
            matchedVersion = ArtifactoryVersion.getCurrent();
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampString);
        } catch (Exception e) {
            timestamp = 0;
        }

        if (matchedVersion == null) {
            matchedVersion = findByVersionString(versionString, revisionString);
            if (matchedVersion != null) {
                log.warn("Closest matched version: {}", matchedVersion.getValue());
            }
        }
        if (matchedVersion == null) {
            matchedVersion = findClosestMatch(versionString, revisionString);
            if (matchedVersion != null) {
                log.warn("Closest matched version: {}", matchedVersion.getValue());
            }
        }

        if (matchedVersion == null) {
            throw new IllegalStateException("No version declared is higher than " + revisionString);
        }

        return new CompoundVersionDetails(matchedVersion, versionString, revisionString, timestamp);
    }

    private static ArtifactoryVersion findByVersionString(String versionString, String revisionString) {
        int artifactoryRevision = Integer.parseInt(revisionString);
        for (ArtifactoryVersion version : ArtifactoryVersion.values()) {
            if (version.getValue().equals(versionString)) {
                if (artifactoryRevision != version.getRevision()) {
                    log.warn("Version found is " + version + " but the revision " +
                            artifactoryRevision + " is not the one supported!\n" +
                            "Reading the folder may work with this version.\n" +
                            "For Information: Using the Command Line Tool is preferable in this case.");
                }
                return version;
            }
        }
        return null;
    }

    private static ArtifactoryVersion findClosestMatch(String versionString, String revisionString) {
        int artifactoryRevision = Integer.parseInt(revisionString);
        log.warn("Version " + versionString + " is not an official release version. " +
                "The closest released revision to " + artifactoryRevision + " will be used to determine the current " +
                "version.\nWarning: This version is unsupported! Reading backup data may not work!\n");
        ArtifactoryVersion[] values = ArtifactoryVersion.values();
        for (int i = values.length - 1; i >= 0; i--) {
            ArtifactoryVersion version = values[i];
            if (artifactoryRevision >= version.getRevision()) {
                return version;
            }
        }
        return null;
    }

    public static CompoundVersionDetails read(File propertiesFile) {
        if (propertiesFile == null) {
            throw new IllegalArgumentException("Null properties file is not allowed");
        }
        try {
            return read(new FileInputStream(propertiesFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Properties file " + propertiesFile.getName() + " doesn't exist");
        }
    }
}
