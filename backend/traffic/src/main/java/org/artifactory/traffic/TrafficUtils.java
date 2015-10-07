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

package org.artifactory.traffic;

import java.util.Date;

/**
 * Utility class for the traffic module
 *
 * @author Noam Tenne
 */
public abstract class TrafficUtils {
    private TrafficUtils() {
        // utility class
    }

    /**
     * Performs a comparison that checks if the date to compare is equals or before the date we want to compare to
     *
     * @param to Compare Date to compare
     * @param to Compare to
     * @return boolean - If the date to compare is equals or before the date we compare to
     */
    public static boolean dateEqualsBefore(Date toCompare, Date to) {
        return toCompare.compareTo(to) < 1;
    }

    /**
     * Performs a comparison that checks if the date to compare is equals or after the date we want to compare to
     *
     * @param to Compare Date to compare
     * @param to Compare to
     * @return boolean - If the date to compare is equals or after the date we compare to
     */
    public static boolean dateEqualsAfter(Date toCompare, Date to) {
        return toCompare.compareTo(to) > -1;
    }
}
