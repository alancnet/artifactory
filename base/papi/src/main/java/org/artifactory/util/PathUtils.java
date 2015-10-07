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

import org.apache.commons.lang.StringUtils;
import org.artifactory.repo.RepoPath;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Path utils
 * <p/>
 * NOTE: INTERNAL USE ONLY - NOT PART OF THE PUBLIC API!
 * <p/>
 * User: freds Date: Aug 3, 2008 Time: 5:42:55 PM
 */
public class PathUtils {

    private static final Pattern PATTERN_SLASHES = Pattern.compile("/+");

    /**
     * Check that the given CharSequence is neither <code>null</code> nor of length 0. Note: Will return
     * <code>true</code> for a CharSequence that purely consists of whitespace.
     * <p><pre>
     * StringUtils.hasLength(null) = false
     * StringUtils.hasLength("") = false
     * StringUtils.hasLength(" ") = true
     * StringUtils.hasLength("Hello") = true
     * </pre>
     *
     * @param str the CharSequence to check (may be <code>null</code>)
     * @return <code>true</code> if the CharSequence is not null and has length
     * @see #hasText(String)
     */
    public static boolean hasLength(String str) {
        return (str != null && str.length() > 0);
    }

    /**
     * Check whether the given CharSequence has actual text. More specifically, returns <code>true</code> if the string
     * not <code>null</code>, its length is greater than 0, and it contains at least one non-whitespace character.
     * <p><pre>
     * StringUtils.hasText(null) = false
     * StringUtils.hasText("") = false
     * StringUtils.hasText(" ") = false
     * StringUtils.hasText("12345") = true
     * StringUtils.hasText(" 12345 ") = true
     * </pre>
     *
     * @param str the CharSequence to check (may be <code>null</code>)
     * @return <code>true</code> if the CharSequence is not <code>null</code>, its length is greater than 0, and it does
     *         not contain whitespace only
     * @see java.lang.Character#isWhitespace
     */
    public static boolean hasText(String str) {
        if (!hasLength(str)) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Trim leading and trailing whitespace from the given String.
     *
     * @param str the String to check
     * @return the trimmed String
     * @see java.lang.Character#isWhitespace
     */
    public static String trimWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuffer buf = new StringBuffer(str);
        while (buf.length() > 0 && Character.isWhitespace(buf.charAt(0))) {
            buf.deleteCharAt(0);
        }
        while (buf.length() > 0 && Character.isWhitespace(buf.charAt(buf.length() - 1))) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    /**
     * @param path A file like path.
     * @return The name of the file from the path)
     */
    public static String getFileName(String path) {
        if (path == null) {
            return null;
        }
        path = normalizePath(path);
        if (path.length() == 0) {
            return "";
        }
        int index = path.lastIndexOf("/");// returns -1 if n '/' exists
        return path.substring(index + 1);
    }

    public static String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        path = replaceBackslashesWithSlashes(path);
        path = PathUtils.trimSlashes(path).toString();
        path = removeDuplicateSlashes(path);
        return path;
    }

    public static String removeDuplicateSlashes(String pathname) {
        if (pathname == null) {
            return null;
        }

        while (pathname.contains("//")) {
            pathname = pathname.replace("//", "/");
        }
        return pathname;
    }

    public static String replaceBackslashesWithSlashes(String path) {
        if (path == null) {
            return null;
        }
        return StringUtils.replace(path, "\\", "/");
    }

    /**
     * @param path A file path
     * @return Parent path of the input path as if it was a file. Empty string if the path has no parent.
     */
    public static String getParent(String path) {
        if (path == null) {
            return null;
        }
        File dummy = new File(path);
        return formatPath(dummy.getParent());
    }

    /**
     * @param path The path (usually of a file)
     * @return The file extension. Null if file name has no extension. For example 'file.xml' will return xml, 'file'
     *         will return null.
     */
    public static String getExtension(String path) {
        if (path == null) {
            return null;
        }
        // TODO: check there is no slash after this dot
        int dotPos = path.lastIndexOf('.');
        if (dotPos < 0) {
            return null;
        }
        return path.substring(dotPos + 1);
    }

    /**
     * @param path The path (usually of a file)
     * @return The path without the extension. If the path has no extension the same path will be returned.
     *         <p/>
     *         For example 'file.xml' will return 'file', 'file' will return 'file'.
     */
    public static String stripExtension(String path) {
        String result = path;
        String extension = getExtension(path);
        if (extension != null) {
            result = path.substring(0, path.length() - extension.length() - 1);
        }
        return result;
    }

    public static String collectionToDelimitedString(Iterable<String> iterable) {
        return collectionToDelimitedString(iterable, ",");
    }

    public static String collectionToDelimitedString(Iterable<String> iterable, String delim) {
        if (iterable == null) {
            return "";
        }
        Iterator<String> it = iterable.iterator();
        if (it == null || !it.hasNext()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            String next = it.next();
            String str = next != null ? next : null;
            if (str == null) {
                continue;
            }
            str = str.trim();
            if (str.length() == 0) {
                continue;
            }
            sb.append(str);
            if (it.hasNext()) {
                sb.append(delim);
            }
        }
        return sb.toString();
    }

    public static List<String> includesExcludesPatternToStringList(String str) {
        return delimitedListToStringList(str, ",", "\r\n\f ");
    }

    public static List<String> delimitedListToStringList(String str, String delimiter) {
        return delimitedListToStringList(str, delimiter, "\r\n\f\t ");
    }

    public static List<String> delimitedListToStringList(String str, String delimiter, String charsToDelete) {
        List<String> result = new ArrayList<>();
        if (str == null) {
            return result;
        }
        if (delimiter == null) {
            result.add(str);
            return result;
        }
        if ("".equals(delimiter)) {
            for (int i = 0; i < str.length(); i++) {
                result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
            }
        } else {
            int pos = 0;
            int delPos;
            while ((delPos = str.indexOf(delimiter, pos)) != -1) {
                result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
                pos = delPos + delimiter.length();
            }
            if (str.length() > 0 && pos <= str.length()) {
                // Add rest of String, but not in case of empty input.
                result.add(deleteAny(str.substring(pos), charsToDelete));
            }
        }
        return result;
    }

    public static String deleteAny(String inString, String charsToDelete) {
        if (!hasLength(inString) || !hasLength(charsToDelete)) {
            return inString;
        }
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < inString.length(); i++) {
            char c = inString.charAt(i);
            if (charsToDelete.indexOf(c) == -1) {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static String formatRelativePath(String path) {
        path = formatPath(path);
        //Trim leading (caused by webdav requests) and trailing '/''s
        CharSequence trimmed = trimSlashes(path);
        return trimmed != null ? trimmed.toString() : null;
    }

    /**
     * Replaces all backslashes in the path to slashes.
     *
     * @param path A path to format
     * @return The input path with all backslashes replaced with slashes. Return empty string if the input path is
     *         null.
     */
    public static String formatPath(String path) {
        if (PathUtils.hasText(path)) {
            path = path.replace('\\', '/');
            return normalizeSlashes(path).toString();
        } else {
            return "";
        }
    }

    // todo: change to return string
    public static CharSequence trimSlashes(CharSequence path) {
        if (path == null) {
            return null;
        }
        path = trimLeadingSlashChars(path);
        path = trimTrailingSlashesChars(path);
        return path;
    }

    public static String trimLeadingSlashes(CharSequence path) {
        CharSequence res = trimLeadingSlashChars(path);
        return res != null ? res.toString() : null;
    }

    /**
     * Convert any sequence of slashses to a single slash (/)
     *
     * @param path
     * @return The normalized path
     */
    public static CharSequence normalizeSlashes(CharSequence path) {
        if (path == null) {
            return null;
        }
        return PATTERN_SLASHES.matcher(path).replaceAll("/");
    }

    public static CharSequence trimLeadingSlashChars(CharSequence path) {
        if (path == null) {
            return null;
        }
        //Trim leading '/' (caused by webdav requests)
        if (path.length() > 0 && path.charAt(0) == '/') {
            path = path.subSequence(1, path.length());
            return trimLeadingSlashChars(path);
        }
        return path;
    }

    public static String trimTrailingSlashes(CharSequence path) {
        CharSequence res = trimTrailingSlashesChars(path);
        return res != null ? res.toString() : null;
    }

    public static CharSequence trimTrailingSlashesChars(CharSequence path) {
        if (path == null) {
            return null;
        }
        if (path.length() > 0 && path.charAt(path.length() - 1) == '/') {
            path = path.subSequence(0, path.length() - 1);
            return trimTrailingSlashes(path);
        }
        return path;
    }

    /**
     * Adds a slash to the end of the path if it doesn't already end with a slash. Whitespaces are also removed with
     * {@link org.artifactory.util.PathUtils#trimWhitespace(String)}.
     * <pre>
     * addTrailingSlash("/acb") = "abc/"
     * addTrailingSlash("/acb   ") = "abc/"
     * addTrailingSlash("/acb/") = "/abc/"
     * addTrailingSlash("") = "/"
     * addTrailingSlash(null) = null
     * </pre>
     *
     * @param path The path to add the trailing slash
     * @return A path with trailing slash at the end
     */
    public static String addTrailingSlash(String path) {
        if (path == null) {
            return null;
        }

        path = trimWhitespace(path);
        return path.endsWith("/") ? path : path + "/";
    }

    @SuppressWarnings({"StringEquality"})
    public static boolean safeStringEquals(String s1, String s2) {
        return s1 == s2 || (s1 != null && s1.equals(s2));
    }

    public static String getRelativePath(String parentPath, String childPath) {
        if (childPath.startsWith("/")) {
            childPath = childPath.substring(1);
        }
        childPath = childPath.substring(parentPath.length(), childPath.length());
        childPath = formatRelativePath(childPath);
        return childPath;
    }

    /**
     * Inhects a string into another string at the specified location.
     * <pre>
     * injectString("Arttory", "ifac", 3) = "Artifactory"
     * injectString("rtifactory", "A", 0) = "Artifactory"
     * injectString("Artifactor", "y", 10) = "Artifactory"
     * injectString("Artifactory", "", 15) = "Artifactory"
     * </pre>
     *
     * @param str            the string to insert another string to
     * @param toInject       string to inject
     * @param injectionIndex where
     * @return The resulting string
     */
    public static String injectString(String str, String toInject, int injectionIndex) {
        if (!hasText(str) || !hasText(toInject)) {
            return str;
        }

        return str.substring(0, injectionIndex) + toInject + str.substring(injectionIndex);
    }

    /**
     * Returns the path elements of the input path.
     * <pre>
     * getPathElements("/a/b/c") = [a, b, c]
     * getPathElements("a/b/c") = [a, b, c]
     * getPathElements("a/b/c/") = [a, b, c]
     * getPathElements("a") = [a]
     * getPathElements("") = []
     * </pre>
     *
     * @param path The path to parse (can be absolute or relative)
     * @return The path's path elements
     */
    public static String[] getPathElements(String path) {
        if (path == null) {
            return new String[0];
        }
        if (path.startsWith("/")) {
            // we don't want to return empty string as a path element so remove the leading slash
            path = path.substring(1);
        }

        return path.split("/");
    }

    /**
     * Get the ancesstor path based on the specified depth
     *
     * @param path  The path to compute the ncesstor for
     * @param depth The depth
     * @return The ancesstor path
     * @throws IllegalArgumentException If depth is bigger than the number of path compponents
     */
    public static String getAncesstor(String path, int depth) {
        String[] elements = getPathElements(path);
        if (elements.length < depth) {
            throw new IllegalArgumentException("Ancesstor of level " + depth + " does not exist for " + path + ".");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < elements.length - depth; i++) {
            String element = elements[i];
            if (sb.length() > 0) {
                sb.append('/');
            }
            sb.append(element);
        }
        if (path.startsWith("/")) {
            sb.insert(0, '/');
        }
        return sb.toString();
    }

    /**
     * Returns the first path element of the input path.
     * <pre>
     * getFirstPathElement("/a/b/c") = "a"
     * getFirstPathElement("a/b/c") = "a"
     * getFirstPathElement("a") = "a"
     * getFirstPathElement("") = ""
     * </pre>
     *
     * @param path The path to parse (can be absolute or relative)
     * @return The path's path elements
     */
    public static String getFirstPathElement(String path) {
        if (path == null) {
            return null;
        }
        String[] elements = getPathElements(path);
        if (elements.length > 0) {
            return elements[0];
        } else {
            return "";
        }
    }

    /**
     * Returns the last path element of the input path.
     * <pre>
     * getFirstPathElement("/a/b/c") = "c"
     * getFirstPathElement("a/b/c") = "c"
     * getFirstPathElement("a") = "a"
     * getFirstPathElement("") = ""
     * </pre>
     *
     * @param path The path to parse (can be absolute or relative)
     * @return The path's path elements
     */
    public static String getLastPathElement(String path) {
        if (path == null) {
            return null;
        }
        String[] elements = getPathElements(path);
        if (elements.length > 0) {
            return elements[elements.length - 1];
        } else {
            return "";
        }
    }

    /**
     * Strips the first path element from the input path.
     * <pre>
     * stripFirstPathElement("/a/b/c") = "b/c"
     * stripFirstPathElement("a/b/c/") = "b/c/"
     * stripFirstPathElement("a") = ""
     * stripFirstPathElement("/") = ""
     * stripFirstPathElement("") = ""
     * stripFirstPathElement(null) = null
     * </pre>
     *
     * @param path The path to strip from (can be absolute or relative)
     * @return The path without the first path element
     */
    public static String stripFirstPathElement(String path) {
        if (path == null) {
            return null;
        }

        path = trimLeadingSlashes(path);
        int indexOfFirstSlash = path.indexOf('/');
        if (indexOfFirstSlash < 0) {
            return "";
        } else {
            return path.substring(indexOfFirstSlash + 1);
        }
    }

    public static boolean isDirectoryPath(String path) {
        return StringUtils.isEmpty(path) || path.matches(".*[/\\\\]$");
    }

    public static String[] splitZipResourcePathIfExist(String path, boolean recursive) {
        int zipResourceStart = path.indexOf(RepoPath.ARCHIVE_SEP);
        if (zipResourceStart > 0) {
            String zipResourcePath = path.substring(zipResourceStart + 1, path.length());
            if (zipResourcePath.startsWith("/")) {
                // all paths are relative inside the zip, so remove the '/' from the beginning
                zipResourcePath = zipResourcePath.substring(1, zipResourcePath.length());
            }
            String[] subSplit;
            if (recursive) {
                subSplit = splitZipResourcePathIfExist(zipResourcePath, true);
            } else {
                subSplit = new String[]{zipResourcePath};
            }
            String[] result = new String[subSplit.length + 1];
            // remove the zip resource sub path from the main path
            result[0] = path.substring(0, zipResourceStart);
            System.arraycopy(subSplit, 0, result, 1, result.length - 1);
            return result;
        }
        return new String[]{path};
    }
}
