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

package org.artifactory.webapp.wicket.page.build.panel.compare;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.artifactory.api.build.BuildRunComparators;
import org.artifactory.build.BuildRun;
import org.artifactory.common.wicket.util.ListPropertySorter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A custom sorter for the build number column. Needed since the build number might not necessarily be numeric
 *
 * @author Noam Y. Tenne
 */
public class BuildForNameListSorter {

    /**
     * Sorts the given list of builds
     *
     * @param toSort    List to sort
     * @param sortParam Selected sort param
     */
    public static void sort(List<BuildRun> toSort, SortParam sortParam) {
        String sortProperty = sortParam.getProperty();
        boolean ascending = sortParam.isAscending();
        if ("number".equals(sortProperty)) {
            sortManual(toSort, ascending);
        } else {
            ListPropertySorter.sort(toSort, sortParam);
        }
    }

    /**
     * Perform a non-property sort
     *
     * @param toSort    List to sort
     * @param ascending True if the order should be ascending
     */
    public static void sortManual(List<BuildRun> toSort, boolean ascending) {
        Comparator<BuildRun> comparator = BuildRunComparators.getComparatorFor(toSort);
        if (!ascending) {
            comparator = Collections.reverseOrder(comparator);
        }
        Collections.sort(toSort, comparator);
    }

}