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

package org.artifactory.md;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * A factory for creating Properties objects.
 * <p/>
 * Has runtime dependency on the core.
 *
 * @author Yoav Landman
 */
public class PropertiesFactory {
    private static final Logger log = LoggerFactory.getLogger(PropertiesFactory.class);

    private static Constructor<?> ctor;

    static {
        try {
            Class<?> clazz =
                    PropertiesFactory.class.getClassLoader().loadClass(
                            "org.artifactory.model.xstream.fs.PropertiesImpl");
            ctor = clazz.getConstructor();
        } catch (Exception e) {
            log.error("Error creating the properties factory.", e);
        }
    }

    /**
     * @return A new empty properties instance
     */
    public static Properties create() {
        try {
            return (Properties) ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could create properties.", e);
        }
    }
}