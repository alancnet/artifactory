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

package org.artifactory.common.wicket.panel.defaultsubmit;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.artifactory.common.wicket.behavior.JavascriptEvent;
import org.artifactory.common.wicket.contributor.ResourcePackage;
import org.artifactory.common.wicket.util.JavaScriptUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yoav Aharoni
 */
public class DefaultSubmit extends Panel {
    public DefaultSubmit(String id, Component... submitButtons) {
        super(id);
        add(new ResourcePackage(DefaultSubmit.class).addJavaScript());

        Component delegateSubmit = new WebMarkupContainer("delegateSubmit");
        String script = getScript(submitButtons);
        delegateSubmit.add(new JavascriptEvent("onclick", script));
        add(delegateSubmit);
    }

    private String getScript(Component... submitButtons) {
        List<String> ids = new ArrayList<>();
        for (Component submitButton : submitButtons) {
            submitButton.setOutputMarkupId(true);
            ids.add(submitButton.getMarkupId());
        }
        return "return " + JavaScriptUtils.jsFunctionCall("DefaultSubmit.submit", ids);
    }
}
