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

package org.artifactory.logging.version.v1;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.util.XmlUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * TO BE USED ONLY UP TO v210<p/> Early logback config "converter". Replaces the existing config to keep it up to date
 * With different changes.
 *
 * @author Noam Tenne
 */
public class LogbackConfigSwapper implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(LogbackConfigSwapper.class);

    /**
     * Replaces the content of the given logback configuration with the content of the latest
     *
     * @param doc Logback configuration
     */
    @Override
    public void convert(Document doc) {
        //Get the updated config
        InputStream newConfigFile =
                getClass().getResourceAsStream("/META-INF/default/" + ArtifactoryHome.LOGBACK_CONFIG_FILE_NAME);
        if (newConfigFile == null) {
            log.error("Replacement logback configuration file was not found in '/META-INF/default/'.");
            return;
        }

        doc.detachRootElement();
        doc.setRootElement(XmlUtils.parse(newConfigFile).detachRootElement());
    }
}