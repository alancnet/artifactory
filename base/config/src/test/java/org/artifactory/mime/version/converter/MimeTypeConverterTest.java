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

package org.artifactory.mime.version.converter;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

import static org.testng.Assert.fail;

/**
 * Base class for mimetype conversion tests.
 *
 * @author Yossi Shaul
 */
public abstract class MimeTypeConverterTest extends XmlConverterTest {
    protected Element getType(List mimetypes, Namespace namespace, String name) {
        for (Object mimetype : mimetypes) {
            Element mimeTypeElement = (Element) mimetype;
            String type = mimeTypeElement.getAttributeValue("type", namespace);
            if (name.equals(type)) {
                return mimeTypeElement;
            }
        }
        fail("Mime type '" + name + "' not found");
        return null;
    }
}
