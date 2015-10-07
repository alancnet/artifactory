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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.IAjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.util.AjaxUtils;

/**
 * @author Yoav Aharoni
 */
public abstract class TitledAjaxSubmitLink extends TitledSubmitLink implements IAjaxLink {
    protected TitledAjaxSubmitLink(String id) {
        super(id);
    }

    protected TitledAjaxSubmitLink(String id, String title) {
        super(id, title);
    }

    protected <T> TitledAjaxSubmitLink(String id, Form<T> form) {
        super(id, form);
    }

    protected TitledAjaxSubmitLink(String id, IModel titleModel, Form<?> form) {
        super(id, titleModel, form);
    }

    protected TitledAjaxSubmitLink(String id, String title, Form<?> form) {
        super(id, title, form);
    }

    {
        add(new AjaxFormSubmitBehavior(form, "onclick") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                TitledAjaxSubmitLink.this.onSubmit(target, getForm());
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                TitledAjaxSubmitLink.this.onError(target);
            }

            @SuppressWarnings({"RefusedBequest"})
            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new CancelEventIfNoAjaxDecorator(TitledAjaxSubmitLink.this.getAjaxCallDecorator());
            }
        });
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        if ("input".equalsIgnoreCase(tag.getName()) || "button".equalsIgnoreCase(tag.getName())) {
            tag.put("type", "submit");
        }
    }

    protected abstract void onSubmit(AjaxRequestTarget target, Form<?> form);

    protected void onError(AjaxRequestTarget target) {
        AjaxUtils.refreshFeedback(target);
    }

    protected IAjaxCallDecorator getAjaxCallDecorator() {
        return null;
    }

    @Override
    public final void onSubmit() {
    }

    @Override
    public final void onClick(AjaxRequestTarget target) {
        onSubmit(target, getForm());
    }
}
