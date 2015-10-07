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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.build.BuildService;
import org.artifactory.build.BuildRun;
import org.artifactory.common.wicket.component.border.fieldset.FieldSetBorder;
import org.artifactory.common.wicket.component.label.highlighter.Syntax;
import org.artifactory.common.wicket.util.WicketUtils;
import org.jfrog.build.api.Build;

/**
 * Displays the build's JSON representation
 *
 * @author Noam Y. Tenne
 */
public class BuildInfoJsonTabPanel extends Panel {

    @SpringBean
    private BuildService buildService;

    /**
     * Main constructor
     *
     * @param id    ID to assign to the panel
     * @param build Build to display
     */
    public BuildInfoJsonTabPanel(String id, Build build) {
        super(id);

        FieldSetBorder border = new FieldSetBorder("jsonBorder");
        add(border);

        BuildRun buildRun = buildService.getBuildRun(build.getName(), build.getNumber(), build.getStarted());
        String buildJson = buildService.getBuildAsJson(buildRun);
        border.add(WicketUtils.getSyntaxHighlighter("jsonContent", buildJson, Syntax.javascript));
    }
}