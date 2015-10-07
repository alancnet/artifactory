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

package org.artifactory.common.wicket.application;

import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.locator.ResourceStreamLocator;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author Yoav Aharoni
 */
public class NoLocaleResourceStreamLocator extends ResourceStreamLocator {
    private Set<Class<?>> noLocaleClasses = new HashSet<>();

    public void addNoLocaleClass(Class<?> clazz) {
        noLocaleClasses.add(clazz);
    }

    @Override
    public IResourceStream locate(Class<?> clazz, String path, String style, String variation, Locale locale,
            String extension, boolean strict) {

        for (Class<?> noLocaleClass : noLocaleClasses) {
            if (noLocaleClass.isAssignableFrom(clazz)) {
                return locate(clazz, path);
            }
        }
        return super.locate(clazz, path, style, variation, locale, extension, strict);
    }
}
