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

package org.artifactory.common.wicket.component;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;

import java.util.Locale;

/**
 * @author Yoav Landman
 */
public abstract class CreateUpdatePanel<E> extends BaseModalPanel {

    protected E entity;
    protected Form<E> form;
    protected CreateUpdateAction action;

    public CreateUpdatePanel(CreateUpdateAction action, E entity) {
        this.entity = entity;
        this.action = action;
        form = new SecureForm("form", new CompoundPropertyModel<>(entity));
        setOutputMarkupId(true);
    }

    @Override
    protected void onModelChanged() {
        super.onModelChanged();
        //Notify the form
        CompoundPropertyModel model = new CompoundPropertyModel(entity);
        form.setDefaultModel(model);
    }

    @Override
    public String getTitle() {
        return getLocalizer().getString(TITLE_KEY + "." + action.name().toLowerCase(Locale.ENGLISH), this);
    }

    public boolean isCreate() {
        return action.equals(CreateUpdateAction.CREATE);
    }
}
