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

package org.artifactory.util;

/**
 * Created by michaelp on 6/29/15.
 */
public class StringUtils {

    private StringUtils() {;}

    /**
     * Replaces last occurrence of given string
     *
     * @param string string to work on
     * @param from replace candidate
     * @param to replace content
     *
     * @return modified string
     */
    public static String replaceLast(String string, String from, String to) {
        if (!com.google.common.base.Strings.isNullOrEmpty(string)) {
            int lastIndex = string.lastIndexOf(from);
            if (lastIndex < 0) return string;
            String tail = string.substring(lastIndex).replaceFirst(from, to);
            return string.substring(0, lastIndex) + tail;
        }
        return string;
    }
}
