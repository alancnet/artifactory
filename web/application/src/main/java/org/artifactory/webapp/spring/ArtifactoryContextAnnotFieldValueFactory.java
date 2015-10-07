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

package org.artifactory.webapp.spring;

import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.proxy.LazyInitProxyFactory;
import org.apache.wicket.spring.ISpringContextLocator;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.lang.reflect.Field;

/**
 * @author Yoav Landman
 */
public class ArtifactoryContextAnnotFieldValueFactory implements IFieldValueFactory {

    private ISpringContextLocator contextLocator;

    /**
     * @param contextLocator spring context locator
     */
    public ArtifactoryContextAnnotFieldValueFactory(ISpringContextLocator contextLocator) {
        if (contextLocator == null) {
            throw new IllegalArgumentException("[contextLocator] argument cannot be null");
        }
        this.contextLocator = contextLocator;
    }

    @Override
    public Object getFieldValue(Field field, Object fieldOwner) {
        if (supportsField(field)) {
            IProxyTargetLocator locator = new ArtifactoryBeanLocator(field.getType(), contextLocator);
            Object proxy = LazyInitProxyFactory.createProxy(field.getType(), locator);
            return proxy;
        } else {
            return null;
        }
    }

    /**
     * @see org.apache.wicket.injection.IFieldValueFactory#supportsField(java.lang.reflect.Field)
     */
    @Override
    public boolean supportsField(Field field) {
        return field.isAnnotationPresent(SpringBean.class);
    }
}
