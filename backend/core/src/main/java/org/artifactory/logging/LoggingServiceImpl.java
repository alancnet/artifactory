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

package org.artifactory.logging;

import org.apache.commons.io.FileUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.artifactory.logging.version.LoggingVersion;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.update.utils.BackupUtils;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Logging service main implementation
 *
 * @author Noam Y. Tenne
 */
@Service
public class LoggingServiceImpl implements LoggingService {
    private static final Logger log = LoggerFactory.getLogger(LoggingServiceImpl.class);

    @Override
    public void exportTo(ExportSettings settings) {
        // export is handled by the application context (all the etc directory is copied)
    }

    @Override
    public void importFrom(ImportSettings settings) {
        File logFileToImport = new File(settings.getBaseDir(), "etc/logback.xml");
        if (logFileToImport.exists()) {
            try {
                // Backup the target logback file
                File targetEtcDir = ArtifactoryHome.get().getEtcDir();
                File existingLogbackFile = new File(targetEtcDir, "logback.xml");
                if (existingLogbackFile.exists()) {
                    FileUtils.copyFile(existingLogbackFile, new File(targetEtcDir, "logback.original.xml"));
                }
                // Copy file into a temporary working directory
                File workFile = new File(FileUtils.getTempDirectory(), logFileToImport.getName());
                FileUtils.copyFile(logFileToImport, workFile);
                convertAndSave(workFile, settings);
                // Copy the converted file to the target dir
                FileUtils.copyFileToDirectory(workFile, targetEtcDir);
                FileUtils.deleteQuietly(workFile);
            } catch (IOException e) {
                settings.getStatusHolder().error("Failed to import and convert logback file", e, log);
            }
        }
    }

    private void convertAndSave(File from, ImportSettings settings) throws IOException {
        ArtifactoryHome artifactoryHome = ArtifactoryHome.get();
        CompoundVersionDetails source = ContextHelper.get().getVersionProvider().getOriginalHome();

        ArtifactorySystemProperties properties = artifactoryHome.getArtifactoryProperties();

        //Might be first run, protect
        if (source != null) {
            LoggingVersion.values();
            ArtifactoryVersion importedVersion = BackupUtils.findVersion(settings.getBaseDir());
            LoggingVersion originalVersion = importedVersion.getSubConfigElementVersion(LoggingVersion.class);
            originalVersion.convert(from.getParentFile(), from.getParentFile());
        }
    }
}