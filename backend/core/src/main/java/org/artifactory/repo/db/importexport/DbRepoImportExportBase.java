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

import org.artifactory.md.MetadataDefinitionService;
import org.artifactory.mime.MavenNaming;
import org.artifactory.mime.NamingUtils;
import org.artifactory.storage.binstore.service.BinaryStore;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.storage.fs.service.WatchesService;
import org.artifactory.storage.spring.StorageContextHelper;

import java.io.File;

/**
 * Base class for the db real repositories import/export handlers.
 *
 * @author Yossi Shaul
 */
abstract class DbRepoImportExportBase {
    String METADATA_FOLDER = ".artifactory-metadata";

    protected boolean isStorableFolder(String name) {
        return !name.endsWith(METADATA_FOLDER) && !name.startsWith(".svn") &&
                !MavenNaming.NEXUS_INDEX_DIR.equals(name);
    }

    protected boolean isStorableFile(String name) {
        return !name.endsWith(METADATA_FOLDER) && !NamingUtils.isChecksum(name);
    }

    protected BinaryStore getBinaryStore() {
        return StorageContextHelper.get().beanForType(BinaryStore.class);
    }

    protected FileService getFileService() {
        return StorageContextHelper.get().beanForType(FileService.class);
    }

    protected WatchesService getWatchesService() {
        return StorageContextHelper.get().beanForType(WatchesService.class);
    }

    protected File getMetadataContainerFolder(File file) {
        return new File(file.getParentFile(), file.getName() + METADATA_FOLDER);
    }

    protected MetadataDefinitionService getMetadataDefinitionService() {
        return StorageContextHelper.get().beanForType(MetadataDefinitionService.class);
    }
}
