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

package org.artifactory.storage.binstore.service;

import org.artifactory.api.common.BasicStatusHolder;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * TORE: [by FSI] use the nio Path object instead of File.
 * <p/>
 * Date: 12/12/12
 * Time: 2:56 PM
 *
 * @author freds
 */
public interface FileBinaryProvider extends BinaryProvider {
    @Nonnull
    File getBinariesDir();

    @Nonnull
    File getFile(String sha1);

    void prune(BasicStatusHolder statusHolder);

    /**
     * Check for state of the filestore.
     *
     * @return true if all access and permissions to folders are valid, false otherwise
     */
    boolean isAccessible();
}
