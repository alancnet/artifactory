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

package org.artifactory.common.wicket.component.help;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.artifactory.common.wicket.behavior.tooltip.TooltipBehavior;
import org.artifactory.common.wicket.contributor.ResourcePackage;
import org.artifactory.common.wicket.util.WicketUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yoav Aharoni
 */

public class HelpBubble extends Label {
    protected static final String TEMPLATE_FILE = "HelpBubble.html";

    /**
     * Protected constructor for explicitly for a class which overrides the class and would Like to supply the model
     * independantly
     *
     * @param id Wicket id
     */
    protected HelpBubble(String id) {
        super(id);
        init();
    }

    public HelpBubble(String id, String helpMessage) {
        this(id, Model.of(helpMessage));
    }

    public HelpBubble(String id, IModel helpModel) {
        super(id, helpModel);
        init();
    }

    private void init() {
        setEscapeModelStrings(false);
        setOutputMarkupId(true);
        add(ResourcePackage.forJavaScript(TooltipBehavior.class));
    }

    @Override
    public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
        replaceComponentTagBody(markupStream, openTag, getComponentMarkup());
    }

    protected String getComponentMarkup() {
        Map<String, String> variables = new HashMap<>();
        variables.put("message", getTooltipMarkup());
        variables.put("enabled", String.valueOf(isEnabled()));
        variables.put("id", getMarkupId());

        final String text = WicketUtils.readResource(HelpBubble.class, TEMPLATE_FILE);
        return new MapVariableInterpolator(text, variables).toString();
    }

    protected String getTooltipMarkup() {
        return getDefaultModelObjectAsString().replaceAll("\n", "<br/>");
    }
}
