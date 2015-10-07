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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.general.dependency;

import org.artifactory.api.module.ModuleInfo;
import org.artifactory.common.wicket.component.label.highlighter.Syntax;

/**
 * The interface for a build-tool specific dependency declaration generator
 *
 * @author Noam Y. Tenne
 */
public interface DependencyDeclarationProvider {

    /**
     * The syntax type of the declaration content. To be used for the syntax highlighter
     *
     * @return Content syntax type
     */
    Syntax getSyntaxType();

    /**
     * Returns the dependency declaration of the build tool
     *
     * @param moduleInfo Module info
     * @return Dependency declaration text
     */
    String getDependencyDeclaration(ModuleInfo moduleInfo);
}
