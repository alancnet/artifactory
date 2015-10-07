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

package org.artifactory.common.wicket.component.checkbox.styled;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.string.StringValueConversionException;
import org.apache.wicket.util.string.Strings;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.DelegateEventBehavior;
import org.artifactory.common.wicket.contributor.ResourcePackage;
import org.artifactory.common.wicket.model.DelegatedModel;
import org.artifactory.common.wicket.model.Titled;

/**
 *
 */
public class StyledCheckbox extends FormComponentPanel<Boolean> implements Titled {
    private CheckBox checkbox;
    private Component button;
    private String title = null;
    private Component submitButton;

    public StyledCheckbox(String id) {
        super(id);
        init();
    }

    public StyledCheckbox(String id, IModel<Boolean> model) {
        super(id, model);
        init();
    }

    protected void init() {
        setType(Boolean.class);
        super.add(ResourcePackage.forJavaScript(StyledCheckbox.class));
        super.add(new CssClass("styled-checkbox"));

        checkbox = new MyCheckBox("checkbox");
        checkbox.setOutputMarkupId(true);
        add(checkbox);

        button = new CheckboxButton("button");
        add(button);
    }

    /**
     * Get a custom input name to be used as 'name' attribute of the form element
     *
     * @param defaultName default input name
     * @return input name
     */
    protected String getCheckboxInputName(String defaultName) {
        return defaultName;
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
            button.add(new DelegateEventBehavior(ajaxEventBehavior.getEvent(), checkbox));
            checkbox.add(ajaxEventBehavior);
            return this;
        }

        return super.add(behavior);
    }

    public boolean isChecked() {
        return Boolean.TRUE.equals(getDefaultModelObject());
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        checkComponentTag(tag, "input");
        checkComponentTagAttribute(tag, "type", "checkbox");

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
        if (title == null) {
            Object label = null;
            if (getLabel() != null) {
                label = getLabel().getObject();
            }

            if (label == null) {
                label = getLocalizer().getString(getId(), getParent(), getId());
            }
            title = label.toString();
        }
        return title;
    }

    public StyledCheckbox setTitle(String title) {
        this.title = title;
        return this;
    }

    public Component getSubmitButton() {
        return submitButton;
    }

    public void setSubmitButton(Component submitButton) {
        this.submitButton = submitButton;
        submitButton.setOutputMarkupId(true);
    }

    @Override
    public String getInputName() {
        return checkbox.getInputName();
    }

    @Override
    protected Boolean convertValue(String[] value) throws ConversionException {
        return toBoolean(value);
    }

    @Override
    public boolean checkRequired() {
        if (isRequired()) {
            final String input = getInput();
            return input == null && !isInputNullable() && !isEnabledInHierarchy() || !Strings.isEmpty(input);

        }
        return true;
    }

    private static Boolean toBoolean(String[] value) throws ConversionException {
        String tmp = value != null && value.length > 0 ? value[0] : null;
        try {
            return Strings.toBoolean(tmp);
        } catch (StringValueConversionException e) {
            final ConversionException conversionException = new ConversionException(
                    String.format("Invalid boolean input value posted \"%s\"", tmp), e);
            conversionException.setTargetType(Boolean.class);
            throw conversionException;
        }
    }

    private class CheckboxButton extends WebMarkupContainer {
        private CheckboxButton(String id) {
            super(id, new Model());
        }

        @Override
        protected void onComponentTag(ComponentTag tag) {
            super.onComponentTag(tag);
            tag.put("for", checkbox.getMarkupId());

            if (!isEnabled()) {
                if (isChecked()) {
                    tag.put("class", "styled-checkbox styled-checkbox-disabled-checked");
                } else {
                    tag.put("class", "styled-checkbox styled-checkbox-disabled-unchecked");
                }
            } else {
                if (isChecked()) {
                    tag.put("class", "styled-checkbox styled-checkbox-checked");
                } else {
                    tag.put("class", "styled-checkbox styled-checkbox-unchecked");
                }
            }

            if (StyledCheckbox.this.isEnabled()) {
                tag.put("onmouseover", "StyledCheckbox.onmouseover(this);");
                tag.put("onmouseout", "StyledCheckbox.onmouseout(this);");
                tag.put("onclick", "StyledCheckbox.onclick(this);");
                if (submitButton != null) {
                    tag.put("onkeydown",
                            String.format("return StyledCheckbox.onkeydown('%s',event);", submitButton.getMarkupId()));
                }
            }
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && StyledCheckbox.this.isEnabled();
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

    private class MyCheckBox extends CheckBox {
        public MyCheckBox(String id) {
            super(id, new DelegatedModel<Boolean>(StyledCheckbox.this));
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && StyledCheckbox.this.isEnabled();
        }

        @Override
        protected Boolean convertValue(String[] value) throws ConversionException {
            return toBoolean(value);
        }

        @Override
        protected void onComponentTag(ComponentTag tag) {
            super.onComponentTag(tag);
            if (isEnabled()) {
                tag.put("onclick", "StyledCheckbox.update(this);");
            }
        }

        @Override
        public String getInputName() {
            return StyledCheckbox.this.getCheckboxInputName(super.getInputName());
        }
    }
}