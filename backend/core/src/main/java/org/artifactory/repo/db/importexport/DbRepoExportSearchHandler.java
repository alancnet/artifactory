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

package org.artifactory.repo.db.importexport;

import org.apache.commons.io.FileUtils;
import org.artifactory.api.config.ExportSettingsImpl;
import org.artifactory.api.search.SavedSearchResults;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.util.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.artifactory.repo.db.importexport.ImportExportAccumulator.ProgressAccumulatorType.EXPORT;

/**
 * Handles export of search results.
 *
 * @author Yossi Shaul
 */
public class DbRepoExportSearchHandler extends DbExportBase {
    private static final Logger log = LoggerFactory.getLogger(DbRepoExportSearchHandler.class);

    private final SavedSearchResults searchResults;
    private final ExportSettingsImpl baseSettings;

    public DbRepoExportSearchHandler(SavedSearchResults searchResults, ExportSettingsImpl baseSettings) {
        super(new ImportExportAccumulator("export-search-result", EXPORT));
        this.searchResults = searchResults;
        this.baseSettings = baseSettings;
        setExportSettings(createSettingsWithTimestampedBase());
    }

    public MutableStatusHolder export() {
        status.status("Started exporting search result '" + searchResults.getName() + "'.", log);

        createExportDirectory();
        if (status.isError()) {
            return status;
        }

        for (org.artifactory.fs.FileInfo searchResult : searchResults.getResults()) {
            exportFile(searchResult);
        }

        if (settings.isCreateArchive()) {
            createExportZip(status, settings);
        }

        status.status("Finished exporting search result '" + searchResults.getName() + "'.", log);
        return status;
    }

    private void createExportZip(MutableStatusHolder statusHolder, ExportSettings settings) {
        try {
            statusHolder.status("Archiving exported search result '" + searchResults.getName() + "'.", log);
            File tempDir = settings.getBaseDir();
            File tempArchive = new File(tempDir.getParentFile(), settings.getBaseDir().getName() + ".zip");
            // Create the archive
            ZipUtils.archive(tempDir, tempArchive, true);
            //Delete the exploded directory
            FileUtils.deleteDirectory(tempDir);
            //Copy the zip back into the final destination
            if (!tempArchive.getParentFile().equals(baseSettings.getBaseDir())) {
                FileUtils.copyFile(tempArchive, baseSettings.getBaseDir());
                //Delete the temporary zip
                FileUtils.deleteQuietly(tempArchive);
            }
        } catch (IOException e) {
            statusHolder.error("Unable to create zip archive", -1, e, log);
        }
    }

    private ExportSettings createSettingsWithTimestampedBase() {
        File baseDir = baseSettings.getBaseDir();
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss");
        String timestamp = formatter.format(baseSettings.getTime());
        String baseExportName = searchResults.getName() + "-" + timestamp;
        File tmpExportDir = new File(baseDir, baseExportName);
        return new ExportSettingsImpl(tmpExportDir, baseSettings);
    }

    private void createExportDirectory() {
        //Make sure the directory does not already exist
        File exportDir;
        if (settings.isCreateArchive()) {
            exportDir = settings.getBaseDir();
        } else {
            exportDir = settings.getBaseDir();
        }
        try {
            FileUtils.deleteDirectory(exportDir);
        } catch (IOException e) {
            status.error("Failed to delete old temp export directory: " + exportDir.getAbsolutePath(), e,
                    log);
        }
        status.status("Creating temp export directory: " + exportDir.getAbsolutePath(), log);
        try {
            FileUtils.forceMkdir(exportDir);
        } catch (IOException e) {
            status.error("Failed to create temp export dir: " + exportDir.getAbsolutePath(), e, log);
        }
        status.status("Using temp export directory: '" + exportDir.getAbsolutePath() + "'.", log);
    }
}
