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

package org.artifactory.storage.db.search.model;

/**
 * Date: 8/6/11
 * Time: 7:41 PM
 *
 * @author Fred Simon
 */
abstract class DbQueryHelper {
    static final String FORWARD_SLASH = "/";
    static final char SLASH_CHAR = '/';
    static final char STAR = '*';
    static final char QUESTION_MARK = '?';
    static final char PERCENT = '%';
    static final char UNDERSCORE = '_';

    private DbQueryHelper() {
    }

    public static int addPathValue(StringBuilder builder, String pathValue) {
        // First add slash if needed
        if (builder.length() > 0) {
            // Avoid double / char
            addCharIfNeeded(builder, SLASH_CHAR);
        }
        return appendWithWildcards(builder, pathValue);
    }

    public static String convertWildcards(String value) {
        StringBuilder result = new StringBuilder();
        appendWithWildcards(result, value);
        return result.toString();
    }

    public static int appendWithWildcards(StringBuilder builder, String value) {
        int nbSlash = 0;
        for (char c : value.toCharArray()) {
            if (c == STAR) {
                // Avoid double % sign
                addCharIfNeeded(builder, PERCENT);
            } else if (c == QUESTION_MARK) {
                builder.append(UNDERSCORE);
            } else {
                if (c == SLASH_CHAR) {
                    nbSlash++;
                }
                builder.append(c);
            }
        }
        return nbSlash;
    }

    private static void addCharIfNeeded(StringBuilder builder, char toAdd) {
        char lastChar = 0;
        if (builder.length() > 0) {
            lastChar = builder.charAt(builder.length() - 1);
        }
        if (lastChar != toAdd) {
            builder.append(toAdd);
        }
    }

    static boolean hasWildcards(String value) {
        for (char c : value.toCharArray()) {
            if (c == STAR || c == QUESTION_MARK || c == PERCENT || c == UNDERSCORE) {
                return true;
            }
        }
        return false;
    }
}
