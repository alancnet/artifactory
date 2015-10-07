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

package org.artifactory.storage.db.util;

/**
 * Base class for the data access objects.
 *
 * @author Yossi Shaul
 */
public class BaseDao {

    protected final JdbcHelper jdbcHelper;

    public BaseDao(JdbcHelper jdbcHelper) {
        this.jdbcHelper = jdbcHelper;
    }

    public static Long nullIfZero(long id) {
        return (id == 0L) ? null : id;
    }

    // TODO [by FSI] : Looks useless since JDBS doing it no?
    public static long zeroIfNull(Long id) {
        return (id == null) ? 0L : id;
    }

    // TODO [by FSI] : Looks useless since JDBS doing it no?
    public static int zeroIfNull(Integer id) {
        return (id == null) ? 0 : id;
    }

    public static Long nullIfZeroOrNeg(long id) {
        return (id <= 0L) ? null : id;
    }

    public static Integer nullIfZeroOrNeg(int id) {
        return (id <= 0L) ? null : id;
    }

    public static String emptyIfNullOrDot(String path) {
        return (path == null || path.length() == 0
                || (path.length() == 1 && path.charAt(0) == '.')) ? "" : path;
    }

    public static String dotIfNullOrEmpty(String path) {
        return (path == null || path.length() == 0) ? "." : path;
    }

    public static String emptyIfNull(String path) {
        return path == null ? "" : path;
    }

    public static String nullIfEmpty(String path) {
        return (path == null || path.length() == 0) ? null : path;
    }

    public static String enumToString(Enum versionType) {
        if (versionType != null) {
            return versionType.name();
        }
        return null;
    }

    /**
     * Turns boolean to byte value (0, 1) for cross database compatibility.
     *
     * @param bool The boolean value
     * @return 0 if boolean is false, 1 if true
     */
    protected byte booleanAsByte(boolean bool) {
        return (byte) (bool ? 1 : 0);
    }
}
