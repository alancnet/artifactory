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

package org.artifactory.logging.version;

import org.apache.commons.io.FileUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.logging.version.v1.LogbackConfigSwapper;
import org.artifactory.logging.version.v3.LogbackJFrogInfoConverter;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.SubConfigElementVersion;
import org.artifactory.version.VersionComparator;
import org.artifactory.version.XmlConverterUtils;
import org.artifactory.version.converter.XmlConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of the logging configuration versions
 *
 * @author Noam Y. Tenne
 */
public enum LoggingVersion implements SubConfigElementVersion {
    v1(ArtifactoryVersion.v122rc0, ArtifactoryVersion.v304, new LogbackConfigSwapper()),
    v2(ArtifactoryVersion.v310, ArtifactoryVersion.v331, null),
    v3(ArtifactoryVersion.v340, ArtifactoryVersion.getCurrent(), new LogbackJFrogInfoConverter());

    private static final Logger log = LoggerFactory.getLogger(LoggingVersion.class);

    private final VersionComparator comparator;
    private XmlConverter xmlConverter;

    /**
     * Main constructor
     *
     * @param from         Start version
     * @param until        End version
     * @param xmlConverter XML converter required for the specified range
     */
    LoggingVersion(ArtifactoryVersion from, ArtifactoryVersion until, XmlConverter xmlConverter) {
        this.xmlConverter = xmlConverter;
        this.comparator = new VersionComparator(from, until);
        ArtifactoryVersion.addSubConfigElementVersion(this, comparator);
    }

    /**
     * Run the needed conversions
     *
     * @param srcEtcDir the directory in which resides the logback file to convert
     */
    public void convert(File srcEtcDir, File targetEtcDir) throws IOException {
        // First create the list of converters to apply
        List<XmlConverter> converters = new ArrayList<>();

        // All converters of versions above me needs to be executed in sequence
        LoggingVersion[] versions = LoggingVersion.values();
        for (LoggingVersion version : versions) {
            if (version.ordinal() >= ordinal() && version.xmlConverter != null) {
                converters.add(version.xmlConverter);
            }
        }

        if (!converters.isEmpty()) {
            File logbackConfigFile = new File(srcEtcDir, ArtifactoryHome.LOGBACK_CONFIG_FILE_NAME);
            try {
                String result =
                        XmlConverterUtils.convert(converters, FileUtils.readFileToString(logbackConfigFile, "utf-8"));
                backupAndSaveLogback(result, targetEtcDir);
            } catch (IOException e) {
                log.error("Error occurred while converting logback config for conversion: {}.", e.getMessage());
                log.debug("Error occurred while converting logback config for conversion", e);
                throw e;
            }
        }
    }

    @Override
    public VersionComparator getComparator() {
        return comparator;
    }

    /**
     * Creates a backup of the existing logback configuration file and proceeds to save post-conversion content
     *
     * @param result Conversion result
     * @param etcDir directory to which to save the conversion result
     */
    public void backupAndSaveLogback(String result, File etcDir) throws IOException {
        File logbackConfigFile = new File(etcDir, ArtifactoryHome.LOGBACK_CONFIG_FILE_NAME);
        if (logbackConfigFile.exists()) {
            File originalBackup = new File(etcDir, "logback.original.xml");
            if (originalBackup.exists()) {
                FileUtils.deleteQuietly(originalBackup);
            }
            FileUtils.moveFile(logbackConfigFile, originalBackup);
        }

        FileUtils.writeStringToFile(logbackConfigFile, result, "utf-8");
    }

    public static void convert(ArtifactoryVersion from, ArtifactoryVersion target, File path)
            throws IOException {
        boolean foundConversion = false;
        // All converters of versions above me needs to be executed in sequence
        LoggingVersion[] versions = LoggingVersion.values();
        for (LoggingVersion version : versions) {
            if (version.comparator.isAfter(from) && !version.comparator.supports(from)) {
                version.convert(path, path);
            }
        }
        // Write to log only if conversion has been executed
        if (foundConversion) {
            log.info("Ending database conversion from " + from + " to " + target);
        }
    }
}