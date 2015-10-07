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

package org.artifactory.converters;

import org.apache.commons.io.FileUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.property.FatalConversionException;
import org.artifactory.logging.version.LoggingVersion;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Gidi Shabat
 */
public class LoggingConverter implements ArtifactoryConverterAdapter {
    private static final Logger log = LoggerFactory.getLogger(LoggingConverter.class);
    private File path;

    public LoggingConverter(File path) {
        this.path = path;
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        try {
            // Perform the logback conversion here because if we do it after configuration is loaded, we must wait 'till
            // the changes are detected by the watchdog (possibly missing out on important log messages)
            //Might be first run, protect
            if (path.exists()) {
                LoggingVersion.convert(source.getVersion(), target.getVersion(), path);
            }
        } catch (FatalConversionException e) {
            //When a fatal conversion happens fail the context loading
            log.error(
                    "Conversion failed with fatal status.\n" +
                            "You should analyze the error and retry launching " +
                            "Artifactory. Error is: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            //When conversion fails - report and continue - don't fail
            log.error("Failed to execute logging conversion.", e);
        }
    }

    @Override
    public boolean isInterested(CompoundVersionDetails source, CompoundVersionDetails target) {
        return source != null && !source.isCurrent();
    }

    @Override
    public void backup() {
        File loginFile = new File(path, ArtifactoryHome.LOGBACK_CONFIG_FILE_NAME);
        File loginBackupFile = new File(path, ArtifactoryHome.LOGBACK_CONFIG_FILE_NAME + ".back");
        try {
            if (loginBackupFile.exists()) {
                FileUtils.forceDelete(loginBackupFile);
            }
            FileUtils.copyFile(loginFile, loginBackupFile);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Fail to save backup file '" + loginBackupFile.getAbsolutePath() + "' from the login file: '" + loginFile.getAbsolutePath() + "'",
                    e);
        }
    }

    @Override
    public void clean() {
        File loginBackupFile = new File(path, ArtifactoryHome.LOGBACK_CONFIG_FILE_NAME + ".back");
        try {
            if (loginBackupFile.exists()) {
                FileUtils.forceDelete(loginBackupFile);
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Fail to clean backup file '" + loginBackupFile.getAbsolutePath() +
                            "' after success conversion'", e);
        }
    }

    @Override
    public void revert() {
        File loginFile = new File(path, ArtifactoryHome.LOGBACK_CONFIG_FILE_NAME);
        File loginBackupFile = new File(path, ArtifactoryHome.LOGBACK_CONFIG_FILE_NAME + ".back");
        try {
            if (loginBackupFile.exists()) {
                if (loginFile.exists()) {
                    FileUtils.forceDelete(loginFile);
                }
                FileUtils.moveFile(loginBackupFile, loginFile);
            }
        } catch (Exception e) {
            throw new RuntimeException("Fail to revert conversion", e);
        }
    }
}

