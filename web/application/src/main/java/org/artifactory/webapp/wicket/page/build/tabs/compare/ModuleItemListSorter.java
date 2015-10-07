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

package org.artifactory.webapp.wicket.page.build.tabs.compare;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.artifactory.common.wicket.util.ListPropertySorter;
import org.jfrog.build.api.Module;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A custom sorter for the build published module lists.<br> This class is needed since the module's artifact and
 * dependency count aren't accessible through the property sorter
 *
 * @author Noam Y. Tenne
 */
public abstract class ModuleItemListSorter {
    private ModuleItemListSorter() {
        // utility class
    }

    /**
     * Sorts the given list of published modules
     *
     * @param toSort    List to sort
     * @param sortParam Selected sort param
     */
    public static void sort(List<Module> toSort, SortParam sortParam) {
        String sortProperty = sortParam.getProperty();
        boolean ascending = sortParam.isAscending();
        if ("artifacts".equals(sortProperty)) {
            sortManual(toSort, new ModuleArtifactComparator(), ascending);
        } else if ("dependencies".equals(sortProperty)) {
            sortManual(toSort, new ModuleDependencyComparator(), ascending);
        } else {
            ListPropertySorter.sort(toSort, sortParam);
        }
    }

    /**
     * Perform a non-property sort
     *
     * @param toSort     List to sort
     * @param comparator Comparator to sort by
     * @param ascending  True if the order should be ascending
     */
    @SuppressWarnings({"unchecked"})
    private static void sortManual(List toSort, Comparator comparator, boolean ascending) {
        if (!ascending) {
            comparator = Collections.reverseOrder(comparator);
        }
        Collections.sort(toSort, comparator);
    }
}