/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.version.converter.v153;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yoav Luft
 */
public class VirtualCacheCleanupConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(VirtualCacheCleanupConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Adding default virtual cache cleanup");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        if (rootElement.getChild("virtualCacheCleanupConfig") != null) return;
        Element cleanupConfig = rootElement.getChild("cleanupConfig", namespace);
        Element virtualCacheCleanupConfig = new Element("virtualCacheCleanupConfig", namespace);
        Element cronExp = new Element("cronExp", namespace);
        cronExp.setText("0 12 5 * * ?");
        virtualCacheCleanupConfig.addContent(cronExp);
        rootElement.addContent(rootElement.indexOf(cleanupConfig) + 1, virtualCacheCleanupConfig);
    }
}
