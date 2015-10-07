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

package org.artifactory.common.wicket.component.label.tooltip;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.behavior.tooltip.TooltipBehavior;

/**
 * @author Yoav Aharoni
 */
public class TooltipLabel extends Label {
    /**
     * Maximum length of the text in the label. If the text the column will display characters up to the maxLength
     * and trailing dots ('...')
     */
    private int maxLength;

    @WicketProperty
    private transient String tooltip;

    private transient String text;

    public TooltipLabel(String id, String label, int maxLength) {
        this(id, Model.of(label), maxLength);
    }

    public TooltipLabel(String id, IModel model, int maxLength) {
        super(id, model);
        this.maxLength = maxLength;
        add(new TooltipBehavior(new PropertyModel(this, "tooltip")));
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        Object modelObject = getDefaultModelObject();
        if (modelObject == null) {
            text = null;
            tooltip = null;
            return;
        }

        String modelObjectString = modelObject.toString();
        if (modelObjectString.length() > maxLength) {
            text = getDefaultModelObjectAsString(modelObjectString.substring(0, maxLength)) + "...";
            tooltip = getDefaultModelObjectAsString(modelObjectString);
        } else {
            text = getDefaultModelObjectAsString(modelObjectString);
            tooltip = null;
        }
    }

    protected void setText(String text) {
        this.text = text;
    }

    protected void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        replaceComponentTagBody(markupStream, openTag, text);
    }
}
