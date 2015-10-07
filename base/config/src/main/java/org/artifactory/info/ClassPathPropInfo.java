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

package org.artifactory.info;

/**
 * An information group for all the java classpath properties
 *
 * @author Noam Tenne
 */
public class ClassPathPropInfo extends SystemPropInfoGroup {

    public ClassPathPropInfo() {
        super("sun.boot.class.path",
                "java.library.path",
                "java.endorsed.dirs",
                "java.ext.dirs",
                "java.class.path");
    }
}