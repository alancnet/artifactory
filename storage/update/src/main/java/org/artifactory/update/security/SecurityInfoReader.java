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

package org.artifactory.update.security;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.SecurityInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads and converts on-the-fly security descriptor xml files. This should be the only class who reads the security xml
 * files.
 *
 * @author Yossi Shaul
 */
public class SecurityInfoReader {
    private static final Logger log = LoggerFactory.getLogger(SecurityInfoReader.class);

    public SecurityInfo read(File securityFile) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(securityFile);
            return read(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    public SecurityInfo read(InputStream is) {
        String xmlContent;
        try {
            xmlContent = IOUtils.toString(is, "utf-8");
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file content: " + e.getMessage(), e);
        }

        return read(xmlContent);
    }

    public SecurityInfo read(String xmlContent) {
        SecurityVersion xmlSecurityVersion = SecurityVersion.findVersion(xmlContent);
        if (!xmlSecurityVersion.isCurrent()) {
            log.info("Converting security.xml version from '{}' to '{}'", xmlSecurityVersion.toString(),
                    SecurityVersion.getCurrent());
            xmlContent = xmlSecurityVersion.convert(xmlContent);
        }

        XStream xStream = InfoFactoryHolder.get().getSecurityXStream();
        SecurityInfo securityInfo = ((SecurityInfo) xStream.fromXML(xmlContent));
        return securityInfo;
    }
}
