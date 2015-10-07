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

package org.artifactory.version;

import org.artifactory.util.XmlUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;

import java.util.List;

/**
 * @author freds
 * @date Nov 9, 2008
 */
public abstract class XmlConverterUtils {
    // ATTENTION: NO LOGGER IN HERE SINCE IT IS CALLED BY LOGGER CONVERTER => STACK OVERFLOW

    private XmlConverterUtils() {
        // utility class
    }

    public static String convert(List<XmlConverter> converters, String in) {
        // If no converters nothing to do
        if (converters.isEmpty()) {
            return in;
        }
        Document doc = XmlUtils.parse(in);
        for (XmlConverter converter : converters) {
            converter.convert(doc);
        }
        return XmlUtils.outputString(doc);
    }

}
