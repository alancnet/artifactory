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

package org.artifactory.convert;

import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.util.ResourceUtils;
import org.artifactory.util.XmlUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Base class for the xml converters (version, security, metadata, etc.)
 *
 * @author Yossi Shaul
 */
public abstract class XmlConverterTest extends ArtifactoryHomeBoundTest {
    private static final Logger log = LoggerFactory.getLogger(XmlConverterTest.class);

    protected Document convertXml(String resourcePath, XmlConverter converter) throws Exception {
        InputStream is = ResourceUtils.getResource(resourcePath);
        Document doc = XmlUtils.parse(is);
        converter.convert(doc);
        return doc;
    }

    protected void debugContent(Document doc) {
        log.debug(XmlUtils.outputString(doc));
    }
}
