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

package org.artifactory.api.config;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.artifactory.api.common.ImportExportStatusHolder;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.FileExportCallback;
import org.artifactory.sapi.common.FileExportEvent;
import org.artifactory.sapi.common.FileExportInfo;

import java.io.File;
import java.util.Date;
import java.util.Set;

/**
 * @author Yoav Landman
 */
@XStreamAlias("export-settings")
public class ExportSettingsImpl extends ImportExportSettingsImpl implements ExportSettings {

    private boolean ignoreRepositoryFilteringRulesOn = false;
    private boolean createArchive = false;
    private Date time;

    /**
     * Flag that indicates if to export m2 compatible meta data
     */
    private boolean m2Compatible = false;

    private boolean incremental;

    private boolean excludeBuilds;

    /**
     * Callbacks - If we need to perform any special actions before exporting a file
     */
    @XStreamOmitField
    private SetMultimap<FileExportEvent, FileExportCallback> callbacks;

    private File outputFile;

    public ExportSettingsImpl(File baseDir) {
        super(baseDir, new ImportExportStatusHolder());
        time = new Date();
        callbacks = HashMultimap.create();
    }

    public ExportSettingsImpl(File baseDir, ImportExportStatusHolder statusHolder) {
        super(baseDir, statusHolder);
        time = new Date();
        callbacks = HashMultimap.create();
    }

    public ExportSettingsImpl(File baseDir, ExportSettings exportSettings) {
        this(baseDir, exportSettings, (ImportExportStatusHolder) exportSettings.getStatusHolder());
    }

    public ExportSettingsImpl(File baseDir, ExportSettings exportSettings, ImportExportStatusHolder statusHolder) {
        super(baseDir, exportSettings, statusHolder);
        ExportSettingsImpl settings = (ExportSettingsImpl) exportSettings;
        this.ignoreRepositoryFilteringRulesOn = settings.ignoreRepositoryFilteringRulesOn;
        this.createArchive = settings.createArchive;
        this.time = settings.time;
        this.m2Compatible = settings.m2Compatible;
        this.incremental = settings.incremental;
        this.callbacks = settings.callbacks;
        this.excludeBuilds = settings.excludeBuilds;

    }

    @Override
    public boolean isIgnoreRepositoryFilteringRulesOn() {
        return ignoreRepositoryFilteringRulesOn;
    }

    @Override
    public void setIgnoreRepositoryFilteringRulesOn(boolean ignoreRepositoryFilteringRulesOn) {
        this.ignoreRepositoryFilteringRulesOn = ignoreRepositoryFilteringRulesOn;
    }

    @Override
    public boolean isCreateArchive() {
        return createArchive;
    }

    @Override
    public void setCreateArchive(boolean createArchive) {
        this.createArchive = createArchive;
    }

    @Override
    public Date getTime() {
        return time;
    }

    @Override
    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * @return True is the export is incremental. Meaning override target only if exported file or folder is newer.
     */
    @Override
    public boolean isIncremental() {
        return incremental;
    }

    /**
     * Incremental export only writes files and folder that are newer than what's in the target.
     *
     * @param incremental True to use incremental export.
     */
    @Override
    public void setIncremental(boolean incremental) {
        this.incremental = incremental;
    }

    @Override
    public boolean isM2Compatible() {
        return m2Compatible;
    }

    @Override
    public void setM2Compatible(boolean m2Compatible) {
        this.m2Compatible = m2Compatible;
    }

    @Override
    public void addCallback(FileExportCallback callback) {
        if (callbacks == null) {
            callbacks = HashMultimap.create();
        }
        for (FileExportEvent event : callback.triggeringEvents()) {
            callbacks.put(event, callback);
        }
    }

    public void executeCallbacks(FileExportInfo info, FileExportEvent event) {
        if (callbacks != null && callbacks.containsKey(event)) {
            final Set<FileExportCallback> triggered = callbacks.get(event);
            for (FileExportCallback callback : triggered) {
                callback.callback(this, info);
            }
        }
    }

    @Override
    public void cleanCallbacks() {
        if ((callbacks != null) && !callbacks.isEmpty()) {
            for (FileExportCallback callback : callbacks.values()) {
                callback.cleanup();
            }
        }
        callbacks.clear();
    }

    @Override
    public boolean isExcludeBuilds() {
        return excludeBuilds;
    }

    @Override
    public void setExcludeBuilds(boolean excludeBuilds) {
        this.excludeBuilds = excludeBuilds;
    }

    @Override
    public File getOutputFile() {
        return outputFile;
    }

    @Override
    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
}