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

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * General utils class.
 *
 * @author Yossi Shaul
 */
public abstract class CollectionUtils {
    private CollectionUtils() {
        // utility class
    }

    /**
     * @return  True if the input collection is null of empty
     */
    public static boolean isNullOrEmpty(@Nullable Collection c) {
        return c == null || c.isEmpty();
    }

    /**
     * @return  True if the input collection is not empty
     */
    public static boolean notNullOrEmpty(@Nullable Collection c) {
        return !isNullOrEmpty(c);
    }

    /**
     * @return  True if the input array is null of empty
     */
    public static <T> boolean isNullOrEmpty(@Nullable T[] a) {
        return a == null || a.length == 0;
    }

}
