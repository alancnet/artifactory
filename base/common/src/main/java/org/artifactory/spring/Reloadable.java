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

package org.artifactory.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate classes that can be reloaded.
 *
 * @author Tomer Cohen
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Reloadable {

    /**
     * @return The class by which this bean will be identified for dependency management (initAfter()).
     */
    Class<? extends ReloadableBean> beanClass();

    /**
     * List of classes this bean is dependant on. It is guaranteed that this {@link ReloadableBean} convert and init
     * methods will be called after the dependent beans. During shutdown the order is the opposite.
     *
     * @return List of beans that this one depends upon
     */
    Class<? extends ReloadableBean>[] initAfter() default {};
}
