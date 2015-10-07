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

package org.artifactory.update.md.current;

import org.artifactory.fs.MetadataEntryInfo;
import org.artifactory.update.md.MetadataConverter;
import org.artifactory.update.md.MetadataConverterUtils;
import org.artifactory.update.md.MetadataType;

/**
 * Date: 11/7/11
 * Time: 9:51 AM
 *
 * @author Fred Simon
 */
public abstract class ConverterMetadataReaderImpl extends PassThroughMetadataReaderImpl {

    @Override
    protected MetadataEntryInfo createMetadataEntry(String metadataName, String xmlContent) {
        MetadataType metadataType = getMetadataTypeForName(metadataName);
        if (metadataType != null) {
            for (MetadataConverter converter : getConverters()) {
                if (converter.getSupportedMetadataType() == metadataType) {
                    xmlContent = MetadataConverterUtils.convertString(converter, xmlContent);
                    metadataName = converter.getNewMetadataName();
                }
            }
        }
        return createME(metadataName, xmlContent);
    }

    protected abstract MetadataConverter[] getConverters();

    protected abstract MetadataType getMetadataTypeForName(String metadataName);

    @Override
    public final MetadataEntryInfo convertMetadataEntry(MetadataEntryInfo metadataEntryInfo) {
        // Enforce conversion
        return createMetadataEntry(metadataEntryInfo.getMetadataName(), metadataEntryInfo.getXmlContent());
    }
}
