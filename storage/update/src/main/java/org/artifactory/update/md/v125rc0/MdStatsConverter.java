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

package org.artifactory.update.md.v125rc0;

import org.artifactory.update.md.MetadataConverter;
import org.artifactory.update.md.MetadataType;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Extracts the stats info (download count) from an old file metadata - before v1.3.0-beta-3.
 *
 * @author Yossi Shaul
 */
public class MdStatsConverter implements MetadataConverter {
    private static final String STATS_NAME = "artifactory.stats";

    @Override
    public String getNewMetadataName() {
        return STATS_NAME;
    }

    @Override
    public MetadataType getSupportedMetadataType() {
        return MetadataType.stats;
    }

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Element downloadCount = root.getChild("downloadCount");
        if (downloadCount == null) {
            downloadCount = new Element("downloadCount");
            downloadCount.setText("0");
        }
        // rename the root to the stats name
        root.setName(STATS_NAME);
        // remove all childer
        root.removeContent();
        // add the download count
        root.addContent(downloadCount);
    }
}
