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

package org.artifactory.update.md.v230;

import org.artifactory.update.md.MetadataConverter;
import org.artifactory.update.md.MetadataType;
import org.artifactory.update.md.MetadataVersion;
import org.artifactory.update.md.current.ConverterMetadataReaderImpl;

/**
 * Reads and converts metadata from version 2.3.0.
 *
 * @author Noam Y. Tenne
 */
public class MetadataReader230 extends ConverterMetadataReaderImpl {
    @Override
    protected MetadataType getMetadataTypeForName(String metadataName) {
        if ("artifactory-file".equals(metadataName)) {
            return MetadataType.file;
        } else if ("artifactory-folder".equals(metadataName)) {
            return MetadataType.folder;
        } else if ("watchers".equals(metadataName)) {
            return MetadataType.watch;
        } else {
            return null;
        }
    }

    @Override
    protected MetadataConverter[] getConverters() {
        return MetadataVersion.v5.getConverters();
    }
}
