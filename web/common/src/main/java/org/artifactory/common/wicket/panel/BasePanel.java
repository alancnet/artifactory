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

package org.artifactory.common.wicket.panel;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

import java.io.Serializable;

/**
 * @author Yoav Aharoni
 */
public class BasePanel<T extends Serializable> extends Panel {
    public BasePanel(String id) {
        super(id);
    }

    public BasePanel(String id, T object) {
        this(id, new CompoundPropertyModel(object));
    }

    public BasePanel(String id, IModel model) {
        super(id, model);
    }

    {
        setOutputMarkupId(true);
    }

    @SuppressWarnings({"unchecked"})
    public T getPanelModelObject() {
        return (T) getDefaultModelObject();
    }

    @SuppressWarnings({"TypeMayBeWeakened"})
    public void setPanelModelObject(T value) {
        setDefaultModelObject(value);
    }
}
