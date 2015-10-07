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

package org.artifactory.descriptor.reader;

import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.jaxb.JaxbHelper;
import org.artifactory.util.Files;
import org.artifactory.version.ArtifactoryConfigVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Reads and converts on-the-fly artifactory config xml files. This should be the only class who reads the artifactory
 * config xml files.
 *
 * @author Tomer Cohen
 */
public class CentralConfigReader {
    private static final Logger log = LoggerFactory.getLogger(CentralConfigReader.class);

    /**
     * @see CentralConfigReader#read(String)
     */
    public CentralConfigDescriptor read(File configFile) {
        return read(Files.readFileToString(configFile));
    }

    /**
     * Read an artifactory.config.xml and determine if it is up to date. If not convert it.
     *
     * @param artifactoryConfigXml The artifactory config xml to be read.
     * @return The up to date artifactory config xml after conversion (if needed).
     */
    public CentralConfigDescriptor read(String artifactoryConfigXml) {
        ArtifactoryConfigVersion configVersion = verifyCurrentConfigVersion(artifactoryConfigXml);
        if (!configVersion.isCurrent()) {
            log.info("Converting artifactory.config.xml version from '{}' to '{}'",
                    configVersion.toString(), ArtifactoryConfigVersion.getCurrent());
            artifactoryConfigXml = configVersion.convert(artifactoryConfigXml);
        }
        return JaxbHelper.readConfig(artifactoryConfigXml);
    }


    /**
     * Sanity check the config file version, in case it was determined that the installed version is current or no
     * conversions were run for other reasons, but the file still has versioning problems.
     *
     * @param configName      Where the data comes from
     * @param configXmlString The all XML data as a string
     */
    private ArtifactoryConfigVersion verifyCurrentConfigVersion(String configXmlString) {
        if (!configXmlString.contains(Descriptor.NS)) {
            String msg = "The current Artifactory config schema namespace is '" + Descriptor.NS +
                    "' The provided config does not seem to be compliant with it.";
            if (log.isDebugEnabled()) {
                log.debug(msg + "\n" + configXmlString);
            } else {
                log.info(msg);
            }
            ArtifactoryConfigVersion guessedConfigVersion = ArtifactoryConfigVersion.getConfigVersion(configXmlString);
            if (guessedConfigVersion == null) {
                throw new RuntimeException(msg +
                        "\nThe auto discovery of Artifactory configuration version " +
                        "did not find any valid version for the artifactory.config.xml file.\n" +
                        "Please fix this file manually!");

            } else if (guessedConfigVersion == ArtifactoryConfigVersion.getCurrent()) {
                throw new RuntimeException(msg +
                        "\nThe auto discovery of Artifactory configuration version found that the " +
                        "artifactory.config.xml file is up to date but does not have the right schema.\n" +
                        "Please fix this file manually!");
            } else {
                return ArtifactoryConfigVersion.getConfigVersion(configXmlString);
            }
        } else {
            return ArtifactoryConfigVersion.getConfigVersion(configXmlString);
        }
    }

}


