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

package org.artifactory.sapi.common;

import org.artifactory.common.MutableStatusHolder;
import org.slf4j.Logger;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Date: 8/3/11
 * Time: 4:00 PM
 *
 * @author Fred Simon
 */
public interface BaseSettings extends Serializable {
    String FULL_SYSTEM = "FULL";

    File getBaseDir();

    boolean isIncludeMetadata();

    void setIncludeMetadata(boolean includeMetadata);

    List<String> getRepositories();

    void setRepositories(List<String> repositories);

    boolean isVerbose();

    void setVerbose(boolean verbose);

    /**
     * @return True if the import/export should fail immediately on certain errors.
     */
    boolean isFailFast();

    /**
     * Is set to true certain import/export actions will fail immediately on error instead of just logging the error
     * and continuing with the import/export.
     *
     * @param failFast True to fail fast, false otherwise
     */
    void setFailFast(boolean failFast);

    boolean isFailIfEmpty();

    void setFailIfEmpty(boolean failIfEmpty);

    MutableStatusHolder getStatusHolder();

    boolean isExcludeContent();

    void setExcludeContent(boolean excludeContent);

    void alertFailIfEmpty(String message, Logger log);
}
