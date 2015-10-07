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

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.MetadataEntryInfo;
import org.artifactory.sapi.fs.MetadataReader;
import org.artifactory.util.Files;
import org.artifactory.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author freds
 * @date Nov 13, 2008
 */
public class PassThroughMetadataReaderImpl implements MetadataReader {
    private static final Logger log = LoggerFactory.getLogger(PassThroughMetadataReaderImpl.class);

    @Override
    public List<MetadataEntryInfo> getMetadataEntries(File file, MutableStatusHolder status) {
        if (!file.isDirectory()) {
            status.error("Expecting a directory but got file: " + file.getAbsolutePath(), log);
            return Collections.emptyList();
        }

        String[] metadataFileNames = file.list(new SuffixFileFilter(".xml"));
        if (metadataFileNames == null) {
            status.error("Cannot read list of metadata files from " + file.getAbsolutePath() + ": "
                    + Files.readFailReason(file), log);
            return Collections.emptyList();
        }

        //Import all the xml files within the metadata folder
        List<MetadataEntryInfo> result = Lists.newArrayListWithCapacity(metadataFileNames.length);
        for (String metadataFileName : metadataFileNames) {
            File metadataFile = new File(file, metadataFileName);
            String extension = PathUtils.getExtension(metadataFileName);
            if (!verify(status, metadataFileName, metadataFile, extension)) {
                continue;
            }
            status.debug("Importing metadata from '" + metadataFile.getPath() + "'.", log);

            try {
                // metadata name is the name of the file without the extension
                String metadataName = PathUtils.stripExtension(metadataFileName);
                String xmlContent = FileUtils.readFileToString(metadataFile, "utf-8");
                MetadataEntryInfo metadataEntry = createMetadataEntry(metadataName, xmlContent);
                result.add(metadataEntry);
            } catch (Exception e) {
                status.error("Failed to import xml metadata from '" +
                        metadataFile.getAbsolutePath() + "'.", e, log);
            }
        }
        return result;
    }

    private boolean verify(MutableStatusHolder status, String metadataFileName, File metadataFile, String extension) {
        if (metadataFile.exists() && metadataFile.isDirectory()) {
            //Sanity check
            status.warn("Skipping xml metadata import from '" + metadataFile.getAbsolutePath() +
                    "'. Expected a file but encountered a folder.", log);
            return false;
        }
        if (extension.length() + 1 >= metadataFileName.length()) {
            // No name for file, just extension
            status.warn("Skipping xml metadata import from '" + metadataFile.getAbsolutePath() +
                    "'. The file does not have a name.", log);
            return false;
        }
        return true;
    }

    protected MetadataEntryInfo createMetadataEntry(String metadataName, String xmlContent) {
        return createME(metadataName, xmlContent);
    }

    @Override
    public MetadataEntryInfo convertMetadataEntry(MetadataEntryInfo metadataEntryInfo) {
        // current doesn't need any conversions...
        return metadataEntryInfo;
    }

    public static MetadataEntryInfo createME(String metadataName, String xmlContent) {
        return InfoFactoryHolder.get().createMetadataEntry(metadataName, xmlContent);
    }
}
