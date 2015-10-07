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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.artifactory.mime.version.MimeTypesVersion;
import org.artifactory.version.CompoundVersionDetails;

import java.io.File;

/**
 * @author Gidi Shabat
 */
public class MimeTypeConverter implements ArtifactoryConverterAdapter {
    private final File path;

    public MimeTypeConverter(File path) {
        this.path = path;
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        if (!path.exists()) {
            throw new RuntimeException(
                    "Couldn't start Artifactory. Mime types file is missing: " + path.getAbsolutePath());
        }

        try {
            String mimeTypesXml = Files.toString(path, Charsets.UTF_8);
            MimeTypesVersion mimeTypesVersion = MimeTypesVersion.findVersion(mimeTypesXml);
            if (!mimeTypesVersion.isCurrent()) {
                String result = mimeTypesVersion.convert(mimeTypesXml);
                Files.write(result, path, Charsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute mimetypes conversion", e);
        }
    }

    @Override
    public boolean isInterested(CompoundVersionDetails source, CompoundVersionDetails target) {
        if (!path.exists()) {
            return false;
        }
        try {
            String mimeTypesXml = Files.toString(path, Charsets.UTF_8);
            MimeTypesVersion mimeTypesVersion = MimeTypesVersion.findVersion(mimeTypesXml);
            return !mimeTypesVersion.isCurrent();
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute mimetypes conversion", e);

        }
    }

    @Override
    public void backup() {
        File mimeTypeFile = path;
        File mimeTypeBackupFile = new File(path.getAbsolutePath() + ".back");
        try {
            if (mimeTypeBackupFile.exists()) {
                FileUtils.forceDelete(mimeTypeBackupFile);
            }
            FileUtils.copyFile(mimeTypeFile, mimeTypeBackupFile);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Fail to save backup file '" + mimeTypeBackupFile.getAbsolutePath() + "' from the login file: '" + mimeTypeFile.getAbsolutePath() + "'",
                    e);
        }
    }

    @Override
    public void clean() {
        File mimeTypeBackupFile = new File(path.getAbsolutePath() + ".back");
        try {
            if (mimeTypeBackupFile.exists()) {
                FileUtils.forceDelete(mimeTypeBackupFile);
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Fail to clean backup file '" + mimeTypeBackupFile.getAbsolutePath() +
                            "' after success conversion'", e);
        }
    }

    @Override
    public void revert() {
        File mimeTypeFile = path;
        File mimeTypeBackupFile = new File(path.getAbsolutePath() + ".back");
        try {
            if (mimeTypeBackupFile.exists()) {
                if (mimeTypeFile.exists()) {
                    FileUtils.forceDelete(mimeTypeFile);
                }
                FileUtils.moveFile(mimeTypeBackupFile, mimeTypeFile);
            }
        } catch (Exception e) {
            throw new RuntimeException("Fail to revert conversion", e);
        }
    }
}
