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

package org.artifactory.util;

import org.apache.commons.lang.StringUtils;

import java.nio.file.InvalidPathException;

/**
 * A path validator, used by both the web and the storage
 *
 * @author Shay Yaakov
 */
public abstract class PathValidator {

    // The state when starting to parse the entire path and when we are valid so far
    private static final int STATE_OK = 0;
    // The state when we found a slash ('/')
    private static final int STATE_SLASH = 1;
    // The state when we found a dot ('.')
    private static final int STATE_DOT = 2;
    // The state when we found a space character (' ' even from tab etc)
    private static final int STATE_SPACE = 3;
    // The state when an ampersand is found ('&') only at the start of a token (start of the entire path or after a '/')
    private static final int STATE_AMPERSAND = 4;

    /**
     * Validates the the given path, throws {@link InvalidPathException} in case it's invalid.
     *
     * @param path The path to validate, it is expected to be in unix separators
     */
    public static void validate(String path) {

        if (StringUtils.isBlank(path)) {
            throw new InvalidPathException(path, "Path cannot be blank");
        }

        int state = STATE_OK;
        int len = path.length();
        int pos = 0;

        final char EOF = (char) -1;
        while (pos <= len) {
            char c = pos == len ? EOF : path.charAt(pos);

            // special check for whitespace
            if (c != ' ' && Character.isWhitespace(c)) {
                c = ' ';
            }
            switch (c) {
                case '/':
                case EOF:
                    if (state == STATE_SPACE) {
                        throw new InvalidPathException(path, "Path cannot have a slash after a space");
                    } else if (state == STATE_DOT) {
                        throw new InvalidPathException(path, "Path element cannot end with a dot");
                    } else if (state == STATE_AMPERSAND) {
                        throw new InvalidPathException(path, "Path cannot have single ampersand");
                    }
                    state = STATE_SLASH;
                    break;

                case '&':
                    if (pos == 0 || state == STATE_SLASH) {
                        // We do not allow only & at a path token but it is allowed in general ('test&', '&test')
                        state = STATE_AMPERSAND;
                    }
                    break;

                case '.':
                    if (pos == 0 || state == STATE_SLASH) {
                        state = STATE_DOT;
                    }
                    break;

                case ' ':
                    if (state == STATE_SLASH) {
                        throw new InvalidPathException(path, "Path cannot have a space after a slash");
                    }
                    state = STATE_SPACE;
                    break;

                case '\\':
                case '|':
                case ':':
                case '*':
                case '?':
                case '"':
                    throw new InvalidPathException(path, "Invalid path. '" + c + "' is not a valid name character");

                default:
                    state = STATE_OK;
                    break;
            }

            pos++;
        }
    }
}
