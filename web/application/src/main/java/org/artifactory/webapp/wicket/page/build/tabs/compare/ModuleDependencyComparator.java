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

import org.jfrog.build.api.Dependency;
import org.jfrog.build.api.Module;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * A custom comparator for sorting a build module's dependencies
 *
 * @author Noam Y. Tenne
 */
public class ModuleDependencyComparator implements Comparator<Module>, Serializable {

    @Override
    public int compare(Module module1, Module module2) {
        if ((module1 == null) || (module2 == null)) {
            return 0;
        }

        List<Dependency> module1Dependencies = module1.getDependencies();
        List<Dependency> module2Dependencies = module2.getDependencies();

        if ((module1Dependencies == null) || (module2Dependencies == null)) {
            return 0;
        }

        Integer module1Count = module1Dependencies.size();
        Integer module2Count = module2Dependencies.size();
        return module1Count.compareTo(module2Count);
    }
}