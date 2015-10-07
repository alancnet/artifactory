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

package org.artifactory.webapp.wicket.page.build.tabs;

import org.apache.wicket.markup.html.panel.Panel;
import org.artifactory.webapp.wicket.page.build.tabs.list.BaseModuleArtifactsListPanel;
import org.artifactory.webapp.wicket.page.build.tabs.list.BaseModuleDependenciesListPanel;

/**
 * The base module information panel
 *
 * @author Noam Y. Tenne
 */
public abstract class BaseModuleInfoTabPanel extends Panel {

    protected BaseModuleArtifactsListPanel moduleArtifactsListPanel;
    protected BaseModuleDependenciesListPanel moduleDependenciesListPanel;

    /**
     * Main constructor
     *
     * @param id ID to assign to the panel
     */
    public BaseModuleInfoTabPanel(String id) {
        super(id);
    }

    /**
     * Returns the published module artifacts list panel
     *
     * @param id ID to assign to the panel
     * @return Module artifacts list panel
     */
    protected abstract Panel getModuleArtifactsListPanel(String id);

    /**
     * Returns the published module dependency list panel
     *
     * @param id ID to assign to the panel
     * @return Module dependencies list panel
     */
    protected abstract Panel getModuleDependenciesListPanel(String id);

    /**
     * Adds the module artifacts table
     */
    protected void addArtifactsTable() {
        add(getModuleArtifactsListPanel("artifactsPanel"));
    }

    /**
     * Adds the module dependencies table
     */
    protected void addDependenciesTable() {
        add(getModuleDependenciesListPanel("dependenciesPanel"));
    }
}