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

package org.artifactory.common.wicket.model;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

/**
 * @author Yoav Aharoni
 */
public class DelegatedModel<T> implements IModel<T> {
    private Component component;

    public DelegatedModel(Component component) {
        this.component = component;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public T getObject() {
        return (T) component.getDefaultModelObject();
    }

    @Override
    public void setObject(T object) {
        component.setDefaultModelObject(object);
    }

    @Override
    public void detach() {
    }
}
