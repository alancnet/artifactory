/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.api.governance;

import org.apache.commons.lang.WordUtils;

/**
 * @author mamo
 */
public class BlackDuckUtils {
    public static String sanitizeDescription(String description) {
        String prefix = "Maven GAV: ";
        int i1 = description.indexOf(prefix);
        if (i1 != -1) {
            int i2 = description.indexOf(" ", i1 + prefix.length());
            if (i2 != -1) {
                description = description.substring(0, i1) + description.substring(i2);
            }
        }
        return description;
    }

    public static String camelize(String text) {
        return text != null ? WordUtils.capitalizeFully(text.toLowerCase()) : null;
    }
}
