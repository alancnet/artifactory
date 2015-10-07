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

package org.artifactory.webapp.wicket.page.build.tabs.diff;

import org.apache.wicket.markup.html.panel.Panel;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.util.SetEnableVisitor;

/**
 * @author Shay Yaakov
 */
public class DisabledBuildDiffTabPanel extends BaseBuildDiffTabPanel {

    public DisabledBuildDiffTabPanel(String id) {
        super(id, null);
        add(new CssClass("disabled-panel"));

        setEnabled(false);
        visitChildren(new SetEnableVisitor(false));
    }

    @Override
    protected Panel getArtifactsDiffListPanel(String id) {
        artifactsDiffListPanel = new DisabledArtifactsDiffListPanel(id);
        return artifactsDiffListPanel;
    }

    @Override
    protected Panel getDependenciesDiffListPanel(String id) {
        dependenciesDiffListPanel = new DisabledDependenciesDiffListPanel(id);
        return dependenciesDiffListPanel;
    }

    @Override
    protected Panel getEnvDiffListPanel(String id) {
        envDiffListPanel = new DisabledPropertiesDiffListPanel(id);
        return envDiffListPanel;
    }
}
