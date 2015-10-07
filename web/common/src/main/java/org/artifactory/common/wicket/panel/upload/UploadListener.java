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

package org.artifactory.common.wicket.panel.upload;

import java.io.File;

public interface UploadListener {
    /**
     * Registers an informational feedback message for this component
     *
     * @param message The feedback message
     */
    void info(final String message);

    /**
     * Callback to activate needed UI on exception/validation errors
     */
    void onException();

    /**
     * Callback method activated once the file is sucessfully saved
     *
     * @param file
     */
    void onFileSaved(File file);
}