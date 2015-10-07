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

package org.artifactory.update.md;

import org.apache.commons.io.IOUtils;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.fs.MetadataEntryInfo;
import org.artifactory.sapi.fs.MetadataReader;
import org.artifactory.update.md.current.PassThroughMetadataReaderImpl;
import org.artifactory.update.md.v125rc0.MdFileConverter;
import org.artifactory.update.md.v125rc0.MdFolderConverter;
import org.artifactory.update.md.v125rc0.MdStatsConverter;
import org.artifactory.update.md.v125rc0.MetadataReader125;
import org.artifactory.update.md.v130beta3.ArtifactoryFileConverter;
import org.artifactory.update.md.v130beta3.ArtifactoryFolderConverter;
import org.artifactory.update.md.v130beta3.MetadataReader130beta3;
import org.artifactory.update.md.v130beta6.ChecksumsConverter;
import org.artifactory.update.md.v130beta6.FolderAdditionalInfoNameConverter;
import org.artifactory.update.md.v130beta6.MetadataReader130beta6;
import org.artifactory.update.md.v230.BaseRepoPathClassConverter;
import org.artifactory.update.md.v230.MetadataReader230;
import org.artifactory.util.XmlUtils;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.SubConfigElementVersion;
import org.artifactory.version.VersionComparator;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author freds
 * @date Nov 11, 2008
 */
public enum MetadataVersion implements MetadataReader, SubConfigElementVersion {
    notSupported(ArtifactoryVersion.v122rc0, ArtifactoryVersion.v122, null),
    v1(ArtifactoryVersion.v125rc0, ArtifactoryVersion.v130beta2, new MetadataReader125(),
            new MdFolderConverter(), new MdFileConverter(), new MdStatsConverter()),
    v2(ArtifactoryVersion.v130beta3, ArtifactoryVersion.v130beta5, new MetadataReader130beta3(),
            new ArtifactoryFolderConverter(), new ArtifactoryFileConverter()),
    v3(ArtifactoryVersion.v130beta6, ArtifactoryVersion.v130beta61, new MetadataReader130beta6(),
            new FolderAdditionalInfoNameConverter(), new ChecksumsConverter()),
    v4(ArtifactoryVersion.v130rc1, ArtifactoryVersion.v225, new PassThroughMetadataReaderImpl()),
    v5(ArtifactoryVersion.v230, ArtifactoryVersion.v2341, new MetadataReader230(),
            new BaseRepoPathClassConverter.FileRepoPathClassConverter(),
            new BaseRepoPathClassConverter.FolderRepoPathClassConverter(),
            new BaseRepoPathClassConverter.WatchersRepoPathClassConverter()),
    v6(ArtifactoryVersion.v240, ArtifactoryVersion.getCurrent(), new PassThroughMetadataReaderImpl());

    private static final Logger log = LoggerFactory.getLogger(MetadataVersion.class);

    private static final String FILE_MD_NAME_V130_BETA_3 = ArtifactoryFileConverter.ARTIFACTORY_FILE + ".xml";
    private static final String FOLDER_MD_NAME_V130_BETA_3 = ArtifactoryFolderConverter.ARTIFACTORY_FOLDER + ".xml";
    private static final String FILE_MD_NAME_V130_BETA_6 = ChecksumsConverter.ARTIFACTORY_FILE + ".xml";
    private static final String FOLDER_MD_NAME_V130_BETA_6 =
            FolderAdditionalInfoNameConverter.ARTIFACTORY_FOLDER + ".xml";

    private final VersionComparator comparator;
    private final MetadataReader delegate;
    private final MetadataConverter[] converters;

    /**
     * @param from       The artifactory version this metadata format was first used in
     * @param until      The latest artifactory version this metadata format was valid
     * @param converters A list of converters to use to convert the metadata from this version range to the next
     */
    MetadataVersion(ArtifactoryVersion from, ArtifactoryVersion until, MetadataReader delegate,
            MetadataConverter... converters) {
        this.comparator = new VersionComparator(from, until);
        this.delegate = delegate;
        this.converters = converters;
    }

    public boolean isCurrent() {
        return comparator.isCurrent();
    }

    public boolean supports(ArtifactoryVersion version) {
        return comparator.supports(version);
    }

    public MetadataConverter[] getConverters() {
        return converters;
    }

    @Override
    public VersionComparator getComparator() {
        return comparator;
    }

    @Override
    public List<MetadataEntryInfo> getMetadataEntries(File file, MutableStatusHolder status) {
        if (delegate == null) {
            throw new IllegalStateException("Metadata Import from version older than 1.2.2 is not supported!");
        }
        // The first delegate provide the base list of metadata entries to convert
        List<MetadataEntryInfo> metadataEntries = delegate.getMetadataEntries(file, status);
        if (!isCurrent()) {
            // All the version above this should be called to convert the metadata entries
            List<MetadataEntryInfo> result = new ArrayList<>(metadataEntries.size());
            MetadataVersion[] values = values();
            for (MetadataEntryInfo metadataEntry : metadataEntries) {
                for (int i = ordinal() + 1; i < values.length; i++) {
                    MetadataVersion value = values[i];
                    metadataEntry = value.convertMetadataEntry(metadataEntry);
                }
                result.add(metadataEntry);
            }
            return result;
        } else {
            return metadataEntries;
        }
    }

    @Override
    public MetadataEntryInfo convertMetadataEntry(MetadataEntryInfo metadataEntryInfo) {
        if (delegate == null) {
            throw new IllegalStateException("Metadata Import from version older than 1.2.2 is not supported!");
        }
        return delegate.convertMetadataEntry(metadataEntryInfo);
    }

    /**
     * Find the version from the format of the metadata folder
     *
     * @param metadataFolder
     */
    public static MetadataVersion findVersion(File metadataFolder) {
        if (!metadataFolder.exists()) {
            throw new IllegalArgumentException(
                    "Cannot find metadata version of non existent file " + metadataFolder.getAbsolutePath());
        }
        if (!metadataFolder.isDirectory()) {
            // For v125rc0 to v130beta2, the folder is actually a file
            return v1;
        }
        File[] mdFiles = metadataFolder.listFiles();
        for (File mdFile : mdFiles) {
            String mdFileName = mdFile.getName();
            if (mdFileName.equalsIgnoreCase(FILE_MD_NAME_V130_BETA_3) ||
                    mdFileName.equalsIgnoreCase(FOLDER_MD_NAME_V130_BETA_3)) {
                return v2;
            }
            if (mdFileName.equalsIgnoreCase(FILE_MD_NAME_V130_BETA_6) ||
                    mdFileName.equalsIgnoreCase(FOLDER_MD_NAME_V130_BETA_6)) {
                // here we don't know if it's beta6 or rc1+ so we must read the xml and decide by the content
                // must improve this in the future.
                Document doc;
                try {
                    doc = buildDocFromFile(mdFile);
                } catch (Exception e) {
                    // log and try the next file
                    log.warn("Failed to read file '" + mdFile + "' as xml", e);
                    continue;
                }
                Element root = doc.getRootElement();
                Element extension = root.getChild("extension");
                if (extension != null) {
                    return v3;
                } else {
                    Element repoPath = root.getChild("repoPath");
                    if (repoPath.getAttribute("class") != null) {
                        return v5;
                    }
                    return v6;
                }
            }
        }
        log.warn("Metadata folder " + metadataFolder.getAbsolutePath() +
                " does not contain any recognizable metadata files! Trying to use the latest metadata layout.");
        return v6;

    }

    private static Document buildDocFromFile(File file) throws Exception {
        InputStream in = new FileInputStream(file);
        try {
            return XmlUtils.parse(in);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public static MetadataVersion findVersion(ArtifactoryVersion version) {
        MetadataVersion result = null;
        MetadataVersion[] metadataVersions = values();
        for (int i = metadataVersions.length - 1; i >= 0; i--) {
            MetadataVersion metadataVersion = metadataVersions[i];
            if (metadataVersion.supports(version)) {
                result = metadataVersion;
                break;
            }
        }
        if (result == null || result == notSupported) {
            throw new IllegalStateException("Metadata import from Artifactory version " + version +
                    " is not supported!");
        }
        return result;
    }
}
