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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Collection of string utils not found in 3rd party libraries.
 *
 * @author Yossi Shaul
 */
public abstract class Strings {
    private Strings() {
        // utility class
    }

    /**
     * Replaces all the characters of the string with asterisks ('*').
     * <pre>
     * mask("acb") = "***"
     * mask("") = ""
     * mask(null) = ""
     * </pre>
     *
     * @param toMask The string to mask
     * @return The masked string
     */
    @Nonnull
    public static String mask(@Nullable String toMask) {
        if (toMask == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(toMask.length());
        for (int i = 0; i < toMask.length(); i++) {
            sb.append('*');
        }
        return sb.toString();
    }

    /**
     * Replaces all the characters of the value with asterisks ('*'). The value is considered to be all the characters
     * after the first equals ('=') character. If there's none the same string is returned.
     * <pre>
     * mask("acb") = "abs"
     * mask("password=123") = "password=***"
     * mask("") = ""
     * mask(null) = ""
     * </pre>
     *
     * @param toMask A string to mask its value
     * @return The masked string.
     */
    @Nonnull
    public static String maskKeyValue(@Nullable String toMask) {
        if (toMask == null) {
            return "";
        }
        int equalsIndex = toMask.indexOf('=');
        if (equalsIndex <= 0) {
            return toMask;
        }

        String key = toMask.substring(0, equalsIndex + 1);
        String value = toMask.substring(equalsIndex + 1, toMask.length());
        StringBuilder sb = new StringBuilder(key);
        for (int i = 0; i < value.length(); i++) {
            sb.append('*');
        }
        return sb.toString();
    }
}
