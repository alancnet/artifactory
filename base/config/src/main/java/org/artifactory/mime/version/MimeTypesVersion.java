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

package org.artifactory.mime.version;

import com.google.common.collect.Lists;
import org.artifactory.mime.version.converter.LatestVersionConverter;
import org.artifactory.mime.version.converter.v1.XmlIndexedConverter;
import org.artifactory.mime.version.converter.v2.AscMimeTypeConverter;
import org.artifactory.mime.version.converter.v3.ArchivesIndexConverter;
import org.artifactory.mime.version.converter.v3.NuPkgMimeTypeConverter;
import org.artifactory.mime.version.converter.v4.GemMimeTypeConverter;
import org.artifactory.mime.version.converter.v5.JsonMimeTypeConverter;
import org.artifactory.mime.version.converter.v6.DebianMimeTypeConverter;
import org.artifactory.mime.version.converter.v7.ArchiveMimeTypeConverter;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.SubConfigElementVersion;
import org.artifactory.version.VersionComparator;
import org.artifactory.version.XmlConverterUtils;
import org.artifactory.version.converter.XmlConverter;

import java.util.Arrays;
import java.util.List;

/**
 * A mimetypes.xml version.
 *
 * @author Yossi Shaul
 */
public enum MimeTypesVersion implements SubConfigElementVersion {
    v1(ArtifactoryVersion.v223, ArtifactoryVersion.v225, new XmlIndexedConverter()),
    v2(ArtifactoryVersion.v230, ArtifactoryVersion.v242, new AscMimeTypeConverter()),
    v3(ArtifactoryVersion.v250, ArtifactoryVersion.v250, new ArchivesIndexConverter(), new NuPkgMimeTypeConverter()),
    v4(ArtifactoryVersion.v251, ArtifactoryVersion.v302, new GemMimeTypeConverter()),
    v5(ArtifactoryVersion.v303, ArtifactoryVersion.v3111, new JsonMimeTypeConverter()),
    v6(ArtifactoryVersion.v320, ArtifactoryVersion.v322, new DebianMimeTypeConverter()),
    v7(ArtifactoryVersion.v330, ArtifactoryVersion.v402, new ArchiveMimeTypeConverter()),
    v8(ArtifactoryVersion.v410, ArtifactoryVersion.getCurrent());

    private final XmlConverter[] converters;

    /**
     * @param start      First Artifactory version that this version was supported.
     * @param end        Last Artifactory version that this version was support.
     * @param converters List of converters to apply for conversion to the next config version.
     */
    MimeTypesVersion(ArtifactoryVersion start, ArtifactoryVersion end, XmlConverter... converters) {
        this.converters = converters;
    }

    /**
     * Convert an xml string to this instance mime type version.
     *
     * @param mimeTypesXmlAsString The mime types xml string to convert
     * @return XML string converted to this version
     */
    public String convert(String mimeTypesXmlAsString) {
        // First create the list of converters to apply
        List<XmlConverter> converters = Lists.newArrayList();

        // All converters of versions above me needs to be executed in sequence
        MimeTypesVersion[] versions = MimeTypesVersion.values();
        for (MimeTypesVersion version : versions) {
            if (version.ordinal() >= ordinal() && version.converters != null) {
                converters.addAll(Arrays.asList(version.converters));
            }
        }
        // Always add the converter that changes the version string
        converters.add(new LatestVersionConverter());

        return XmlConverterUtils.convert(converters, mimeTypesXmlAsString);
    }

    @Override
    public VersionComparator getComparator() {
        throw new UnsupportedOperationException("stop being lazy and implement me");
    }

    /**
     * @param mimeTypesXmlAsString The string representation of the mimetypes.xml file
     * @return The {@link MimeTypesVersion} matching the xml content.
     */
    public static MimeTypesVersion findVersion(String mimeTypesXmlAsString) {
        final String VERSION_ATT = "<mimetypes version=\"";
        int versionIdx = mimeTypesXmlAsString.indexOf(VERSION_ATT);
        if (versionIdx < 0) {
            throw new IllegalArgumentException("Unidentified mimetypes configuration");
        }

        int versionStartIndex = versionIdx + VERSION_ATT.length();
        //TODO: [by YS] this relies on single digit version number which will work up until version 9. Should instead parse the string until next quote
        int version = Integer.parseInt(mimeTypesXmlAsString.substring(versionStartIndex, versionStartIndex + 1));
        if (MimeTypesVersion.values().length < version) {
            throw new IllegalArgumentException("Version " + version + " no found.");
        }
        return MimeTypesVersion.values()[version - 1];
    }

    public static MimeTypesVersion getCurrent() {
        MimeTypesVersion[] versions = MimeTypesVersion.values();
        return versions[versions.length - 1];
    }

    public boolean isCurrent() {
        return this == getCurrent();
    }

    /**
     * @return The version string associated to this version (the one written in the xml file)
     */
    public String versionString() {
        return name().substring(1);
    }
}
