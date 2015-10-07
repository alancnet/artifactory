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

package org.artifactory.version.converter.v162;

import com.google.common.collect.Lists;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Adds the Folder Download config section with default value where applicable
 *
 * @author Dan Feldman
 */
public class FolderDownloadConfigConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(FolderDownloadConfigConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting add default Folder Download config conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element folderDownloadConfigElement = rootElement.getChild("folderDownloadConfig", namespace);
        if (folderDownloadConfigElement == null) {
            log.info("No folder download config found - adding default one");
            addDefaultConfig(rootElement, namespace);
        }
        log.info("Finished add default Folder Download config conversion");
    }

    private void addDefaultConfig(Element rootElement, Namespace namespace) {
        //Use defaults from descriptor
        FolderDownloadConfigDescriptor descriptor = new FolderDownloadConfigDescriptor();
        Element folderDownload = new Element("folderDownloadConfig", namespace);
        Namespace folderConfigNs = folderDownload.getNamespace();
        ArrayList<Element> elements = Lists.newArrayList();
        elements.add(new Element("enabled", folderConfigNs).setText(String.valueOf(descriptor.isEnabled())));
        elements.add(new Element("maxDownloadSizeMb", folderConfigNs).setText(
                String.valueOf(descriptor.getMaxDownloadSizeMb())));
        elements.add(new Element("maxFiles", folderConfigNs).setText(String.valueOf(descriptor.getMaxFiles())));
        elements.add(new Element("maxConcurrentRequests", folderConfigNs).setText(
                String.valueOf(descriptor.getMaxConcurrentRequests())));
        folderDownload.addContent(elements);
        rootElement.addContent(rootElement.getContentSize(), folderDownload);
    }
}
