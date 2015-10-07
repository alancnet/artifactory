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

package org.artifactory.common.wicket.component.links;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.PrependingStringBuffer;

/**
 * @author Yoav Aharoni
 */
public abstract class TitledSubmitLink extends BaseTitledLink implements IFormSubmittingComponent {
    protected Form<?> form;

    protected TitledSubmitLink(String id) {
        this(id, (Form<?>) null);
    }

    protected TitledSubmitLink(String id, String title) {
        this(id, title, null);
    }

    protected TitledSubmitLink(String id, Form<?> form) {
        super(id);
        this.form = form;
    }

    protected TitledSubmitLink(String id, IModel titleModel, Form<?> form) {
        super(id, titleModel);
        this.form = form;
    }

    protected TitledSubmitLink(String id, String title, Form<?> form) {
        super(id, title);
        this.form = form;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        if ("input".equalsIgnoreCase(tag.getName()) || "button".equalsIgnoreCase(tag.getName())) {
            tag.put("type", "submit");
            tag.put("name", getInputName());
        }
    }

    @Override
    public boolean getDefaultFormProcessing() {
        return true;
    }

    @Override
    public Component setDefaultFormProcessing(boolean defaultFormProcessing) {
        return null;
    }

    @Override
    public void onError() {
    }

    @Override
    public final Form<?> getForm() {
        if (form == null) {
            // try to find form in the hierarchy of owning component
            form = findParent(Form.class);
            if (form == null) {
                throw new IllegalStateException(
                        "form was not specified in the constructor and cannot be found in the hierarchy of the TitledSubmitLink");
            }
        }
        return form;
    }

    @Override
    public String getInputName() {
        // TODO: This is a copy & paste from the FormComponent class.
        String id = getId();
        final PrependingStringBuffer inputName = new PrependingStringBuffer(id.length());
        Component c = this;
        while (true) {
            inputName.prepend(id);
            c = c.getParent();
            if (c == null || (c instanceof Form && ((Form) c).isRootForm()) || c instanceof Page) {
                break;
            }
            inputName.prepend(Component.PATH_SEPARATOR);
            id = c.getId();
        }

        // having input name "submit" causes problems with javascript, so we
        // create a unique string to replace it by prepending a path separator
        if ("submit".equals(inputName.toString())) {
            inputName.prepend(Component.PATH_SEPARATOR);
        }
        return inputName.toString();
    }
}
