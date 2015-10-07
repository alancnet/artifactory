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

import org.apache.commons.io.FileUtils;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.fs.MetadataEntryInfo;
import org.artifactory.sapi.fs.MetadataReader;
import org.artifactory.update.md.MetadataConverter;
import org.artifactory.update.md.MetadataConverterUtils;
import org.artifactory.update.md.current.PassThroughMetadataReaderImpl;
import org.artifactory.update.md.v130beta6.ChecksumsConverter;
import org.artifactory.update.md.v130beta6.FolderAdditionalInfoNameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author freds
 * @date Nov 13, 2008
 */
public class MetadataReader125 implements MetadataReader {
    private static final Logger log = LoggerFactory.getLogger(MetadataReader125.class);
    private final MetadataConverter folderConverter = new MdFolderConverter();
    private final MetadataConverter fileConverter = new MdFileConverter();
    private final MetadataConverter statsConverter = new MdStatsConverter();

    @Override
    public List<MetadataEntryInfo> getMetadataEntries(File file, MutableStatusHolder status) {
        if (!file.isFile()) {
            status.error("Expecting a file but got a directory: " + file.getAbsolutePath(), log);
            return Collections.emptyList();
        }
        List<MetadataEntryInfo> result = new ArrayList<>();
        try {
            String xmlContent = FileUtils.readFileToString(file, "utf-8");
            if (xmlContent.contains("<file>")) {
                // convert to file metadata
                String fileXmlContent = MetadataConverterUtils.convertString(fileConverter, xmlContent);
                ChecksumsConverter checksumsConverter = new ChecksumsConverter();
                fileXmlContent = MetadataConverterUtils.convertString(checksumsConverter, fileXmlContent);
                String fileMetadataName = checksumsConverter.getNewMetadataName();
                result.add(PassThroughMetadataReaderImpl.createME(fileMetadataName, fileXmlContent));

                // get the stats data from the original xml content
                result.add(convert(xmlContent, statsConverter));
            } else if (xmlContent.contains("<folder>")) {
                xmlContent = MetadataConverterUtils.convertString(this.folderConverter, xmlContent);
                FolderAdditionalInfoNameConverter folderConverter = new FolderAdditionalInfoNameConverter();
                xmlContent = MetadataConverterUtils.convertString(folderConverter, xmlContent);
                String fileMetadataName = folderConverter.getNewMetadataName();
                result.add(PassThroughMetadataReaderImpl.createME(fileMetadataName, xmlContent));
            } else {
                status.error("Failed to import xml metadata from '" +
                        file.getAbsolutePath() + "' since it does not contain any known XML tag <file> <folder>.",
                        log);
            }
        } catch (IOException e) {
            status.error("Failed to import xml metadata from '" +
                    file.getAbsolutePath() + "'.", e, log);
        }
        return result;
    }

    private MetadataEntryInfo convert(String xmlContent, MetadataConverter converter) {
        MetadataEntryInfo metadataEntry;
        xmlContent = MetadataConverterUtils.convertString(converter, xmlContent);
        metadataEntry = PassThroughMetadataReaderImpl.createME(converter.getNewMetadataName(), xmlContent);
        return metadataEntry;
    }

    @Override
    public MetadataEntryInfo convertMetadataEntry(MetadataEntryInfo metadataEntryInfo) {
        throw new IllegalStateException("Cannot convert the root metadata reader " + this);
    }
}