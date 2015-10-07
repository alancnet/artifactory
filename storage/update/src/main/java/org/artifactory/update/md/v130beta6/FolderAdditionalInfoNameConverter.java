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

package org.artifactory.update.md.v130beta6;

import org.artifactory.update.md.MetadataConverter;
import org.artifactory.update.md.MetadataType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts FolderInfo from version 1.3.0-beta-6. Just renaming extension to additionalInfo.
 *
 * @author Yossi Shaul
 */
public class FolderAdditionalInfoNameConverter implements MetadataConverter {
    private static final Logger log = LoggerFactory.getLogger(FolderAdditionalInfoNameConverter.class);
    public static final String ARTIFACTORY_FOLDER = "artifactory-folder";

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Element extension = root.getChild("extension");
        if (extension != null) {
            extension.setName("additionalInfo");
        } else {
            log.warn("Folder info extension node not found");
        }
    }

    @Override
    public String getNewMetadataName() {
        // metadata name not changed
        return ARTIFACTORY_FOLDER;
    }

    @Override
    public MetadataType getSupportedMetadataType() {
        return MetadataType.folder;
    }

}
