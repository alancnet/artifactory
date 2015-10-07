/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

import org.artifactory.sapi.common.ImportSettings;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Specific settings for repositories import.
 *
 * @author Yoav Luft
 */
public class RepositoryImportSettingsImpl extends ImportSettingsImpl implements ImportSettings {
    private List<String> repositoriesToDelete = Collections.emptyList();
    /**
     * When true this variable implies that only one repository should be imported and the baseDir is the root of this
     * repository.
     */
    private boolean singleRepoImport = false;

    public RepositoryImportSettingsImpl(File baseDir) {
        super(baseDir);
    }

    public RepositoryImportSettingsImpl(File baseDir, ImportSettings settings) {
        super(baseDir, settings);
        if (settings instanceof RepositoryImportSettingsImpl) {
            this.singleRepoImport = ((RepositoryImportSettingsImpl) settings).isSingleRepoImport();
            this.repositoriesToDelete = ((RepositoryImportSettingsImpl) settings).getRepositoriesToDelete();
        }
    }

    public void setRepositoriesToDelete(List<String> repositoriesToDelete) {
        this.repositoriesToDelete = repositoriesToDelete;
    }

    public List<String> getRepositoriesToDelete() {
        return repositoriesToDelete;
    }

    public boolean isSingleRepoImport() {
        return singleRepoImport;
    }

    public void setSingleRepoImport(boolean singleRepoImport) {
        this.singleRepoImport = singleRepoImport;
    }
}
