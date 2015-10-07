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

package org.artifactory.mime;

import com.google.common.collect.ImmutableSet;
import com.sun.xml.internal.ws.api.streaming.XMLStreamWriterFactory;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.mime.version.MimeTypesVersion;
import org.artifactory.util.PrettyStaxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A reader of mimetypes configuration file.
 *
 * @author Yossi Shaul
 */
public class MimeTypesReader {
    private static final Logger log = LoggerFactory.getLogger(MimeTypesReader.class);

    public MimeTypes read(File mimeTypesFile) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(mimeTypesFile);
            return read(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    public MimeTypes read(InputStream is) {
        String xmlContent;
        try {
            xmlContent = IOUtils.toString(is, "utf-8");
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file content: " + e.getMessage(), e);
        }

        return read(xmlContent);
    }

    public MimeTypes read(String xmlContent) {
        MimeTypesVersion mimeTypesVersion = MimeTypesVersion.findVersion(xmlContent);
        if (!mimeTypesVersion.isCurrent()) {
            log.info("Converting mimetypes.xml version from '{}' to '{}'", mimeTypesVersion,
                    MimeTypesVersion.getCurrent());
            xmlContent = mimeTypesVersion.convert(xmlContent);
        }

        XStream xStream = new XStream(new PrettyStaxDriver(null));
        xStream.processAnnotations(new Class[]{MimeTypes.class, MimeType.class});
        xStream.registerLocalConverter(MimeType.class, "extensions", new StringToListAttributeConverter());
        MimeTypes mimeTypes = ((MimeTypes) xStream.fromXML(xmlContent));
        return mimeTypes;
    }

    private static class StringToListAttributeConverter implements SingleValueConverter {
        /**
         * Converts a comma separated string to a list of strings.
         *
         * @param str A comma separated list of strings
         */
        @Override
        public ImmutableSet<String> fromString(String str) {
            if (StringUtils.isBlank(str)) {
                return ImmutableSet.of();
            }

            ImmutableSet.Builder<String> builder = ImmutableSet.builder();
            String[] extensions = str.split(",");
            for (String extension : extensions) {
                extension = StringUtils.trim(extension);
                if (StringUtils.isNotBlank(extension)) {
                    builder.add(extension);
                }
            }
            return builder.build();
        }

        @Override
        public String toString(Object obj) {
            throw new UnsupportedOperationException("Marshaling is not supported");
        }

        @Override
        public boolean canConvert(Class type) {
            throw new UnsupportedOperationException("This method should not be called on a local converter");
        }
    }
}
