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

package org.artifactory.common.wicket.component.radio.styled;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.LabeledWebMarkupContainer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.DelegateEventBehavior;
import org.artifactory.common.wicket.contributor.ResourcePackage;
import org.artifactory.common.wicket.model.DelegatedModel;
import org.artifactory.common.wicket.model.Titled;

/**
 *
 */
public class StyledRadio<T> extends LabeledWebMarkupContainer implements Titled {
    private Radio radio;
    private Component button;

    private RadioGroup<T> group;

    public StyledRadio(String id) {
        super(id);
        init();
    }

    public StyledRadio(String id, IModel model) {
        super(id, model);
        init();
    }

    protected void init() {
        super.add(ResourcePackage.forJavaScript(StyledRadio.class));
        super.add(new CssClass("styled-checkbox"));

        radio = new Radio<>("radio", new DelegatedModel<T>(this));
        radio.setOutputMarkupId(true);
        add(radio);

        button = new RadioButton("button");
        add(button);
    }

    public StyledRadio setGroup(RadioGroup<T> group) {
        this.group = group;
        return this;
    }

    @Override
    public Component add(final Behavior... behaviors) {
        for (Behavior behavior : behaviors) {
            internalAdd(behavior);
        }
        return this;
    }

    private Component internalAdd(Behavior behavior) {
        if (AjaxEventBehavior.class.isAssignableFrom(behavior.getClass())) {
            AjaxEventBehavior ajaxEventBehavior = (AjaxEventBehavior) behavior;
            button.add(new DelegateEventBehavior(ajaxEventBehavior.getEvent(), radio));
            radio.add(ajaxEventBehavior);
            return this;
        }

        return super.add(behavior);
    }

    @SuppressWarnings("unchecked")
    protected RadioGroup<T> getGroup() {
        RadioGroup<T> group = this.group;
        if (group == null) {
            group = findParent(RadioGroup.class);
            if (group == null) {
                throw new WicketRuntimeException(
                        "Radio component [" +
                                getPath() +
                                "] cannot find its parent RadioGroup. All Radio components must be a child of or below in the hierarchy of a RadioGroup component.");
            }
        }
        return group;
    }

    private boolean isChecked() {
        final RadioGroup<T> group = getGroup();
        if (group.hasRawInput()) {
            String rawInput = group.getRawInput();
            if (rawInput != null && rawInput.equals(radio.getValue())) {
                return true;
            }
        } else if (group.getModelComparator().compare(group, getDefaultModelObject())) {
            return true;
        }
        return false;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        checkComponentTag(tag, "input");
        checkComponentTagAttribute(tag, "type", "radio");

        // rename input tag to span tag
        tag.setName("span");
        tag.remove("type");
        tag.remove("value");
        tag.remove("name");
    }

    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        super.onComponentTagBody(markupStream, openTag);

        // close span  tag
        getResponse().write(openTag.syntheticCloseTagString());
        openTag.setType(XmlTag.TagType.CLOSE);
    }

    @Override
    public String getTitle() {
        Object label = null;

        if (getLabel() != null) {
            label = getLabel().getObject();
        }

        if (label == null) {
            label = getLocalizer().getString(getId(), getParent(), getId());
        }

        return label.toString();
    }

    private class RadioButton extends WebMarkupContainer {
        private RadioButton(String id) {
            super(id, new Model());
        }

        @Override
        protected void onComponentTag(ComponentTag tag) {
            super.onComponentTag(tag);
            tag.put("for", radio.getMarkupId());

            if (isChecked()) {
                tag.put("class", "styled-checkbox styled-checkbox-checked");
            } else {
                tag.put("class", "styled-checkbox styled-checkbox-unchecked");
            }
        }

        @SuppressWarnings({"RefusedBequest"})
        @Override
        public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
            replaceComponentTagBody(markupStream, openTag, getHtml());
        }

        private String getHtml() {
            return "<div class='button-center'><div class='button-left'><div class='button-right'>"
                    + getTitle()
                    + "</div></div></div>";
        }
    }
}
