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

package org.artifactory.rest.resource.system;

/**
 * Date: 10/27/11
 * Time: 2:37 PM
 *
 * @author Fred Simon
 */
public class ExportSettingsConfigurationImpl {
    private String exportPath;
    private boolean explicitIncludeMetadata;
    private boolean includeMetadata = true;
    private boolean createArchive;
    private boolean bypassFiltering;
    private boolean verbose;
    private boolean failOnError = false;
    private boolean failIfEmpty = true;
    private boolean m2;
    private boolean incremental;
    private boolean excludeContent;

    public void setIncludeMetadata(boolean includeMetadata) {
        explicitIncludeMetadata = true;
        this.includeMetadata = includeMetadata;
    }

    public boolean isIncludeMetadata() {
        // The following code supports old behaviors. in previous versions excludeMetadata was allowed only if the
        // excludeContent was true
        if (explicitIncludeMetadata) {
            return includeMetadata;
        }
        if (excludeContent) {
            return false;
        }
        return includeMetadata;
    }

    public String getExportPath() {
        return exportPath;
    }

    public void setExportPath(String exportPath) {
        this.exportPath = exportPath;
    }

    public boolean isCreateArchive() {
        return createArchive;
    }

    public void setCreateArchive(boolean createArchive) {
        this.createArchive = createArchive;
    }

    public boolean isBypassFiltering() {
        return bypassFiltering;
    }

    public void setBypassFiltering(boolean bypassFiltering) {
        this.bypassFiltering = bypassFiltering;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean isFailIfEmpty() {
        return failIfEmpty;
    }

    public void setFailIfEmpty(boolean failIfEmpty) {
        this.failIfEmpty = failIfEmpty;
    }

    public boolean isM2() {
        return m2;
    }

    public void setM2(boolean m2) {
        this.m2 = m2;
    }

    public boolean isIncremental() {
        return incremental;
    }

    public void setIncremental(boolean incremental) {
        this.incremental = incremental;
    }

    public boolean isExcludeContent() {
        return excludeContent;
    }

    public void setExcludeContent(boolean excludeContent) {
        this.excludeContent = excludeContent;
    }
}
