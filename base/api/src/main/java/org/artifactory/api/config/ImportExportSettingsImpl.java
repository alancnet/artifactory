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

import org.artifactory.common.MutableStatusHolder;
import org.artifactory.sapi.common.BaseSettings;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * @author Fred Simon
 * @date Sep 29, 2008
 */
public class ImportExportSettingsImpl implements BaseSettings {
    private final File baseDir;
    private boolean includeMetadata = true;
    private boolean verbose;
    private boolean failFast;
    private boolean failIfEmpty;
    private Path archiveTempDir;
    protected boolean excludeContent;
    private MutableStatusHolder statusHolder;
    /**
     * List of repositories to do export or import on. When empty - export or import all
     */
    private List<String> repositories = Collections.emptyList();


    public ImportExportSettingsImpl(File baseDir, MutableStatusHolder statusHolder) {
        this.baseDir = baseDir;
        this.statusHolder = statusHolder;
    }

    public ImportExportSettingsImpl(File baseDir, BaseSettings baseSettings, MutableStatusHolder statusHolder) {
        this(baseDir, statusHolder);
        ImportExportSettingsImpl settings = (ImportExportSettingsImpl) baseSettings;
        this.includeMetadata = settings.includeMetadata;
        this.repositories = settings.repositories;
        this.verbose = settings.verbose;
        this.failFast = settings.failFast;
        this.failIfEmpty = settings.failIfEmpty;
        this.excludeContent = settings.excludeContent;
    }

    /**
     * @return Base directory of the operation (target directory of the export or source directory of an import)
     */
    @Override
    public File getBaseDir() {
        return baseDir;
    }

    @Override
    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    @Override
    public void setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
    }

    /**
     * @return List of repositories to do export or import on. Empty if needs to export or import all.
     */
    @Override
    public List<String> getRepositories() {
        return repositories;
    }

    @Override
    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
        statusHolder.setVerbose(verbose);
    }

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
        statusHolder.setFastFail(failFast);
    }

    @Override
    public boolean isFailIfEmpty() {
        return failIfEmpty;
    }

    @Override
    public void setFailIfEmpty(boolean failIfEmpty) {
        this.failIfEmpty = failIfEmpty;
    }

    @Override
    public MutableStatusHolder getStatusHolder() {
        return statusHolder;
    }

    @Override
    public boolean isExcludeContent() {
        return excludeContent;
    }

    @Override
    public void setExcludeContent(boolean excludeContent) {
        this.excludeContent = excludeContent;
    }

    @Override
    public void alertFailIfEmpty(String message, Logger log) {
        if (isFailIfEmpty()) {
            statusHolder.error(message, log);
        } else {
            statusHolder.warn(message, log);
        }
    }
}
