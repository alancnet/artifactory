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

package org.artifactory.ivy;

import org.apache.commons.lang.StringUtils;

public abstract class IvyNaming {

    public static final String IVY_XML = "ivy.xml";

    private IvyNaming() {
        // utility class
    }

    public static boolean isIvyFileName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return false;
        }
        return IvyNaming.IVY_XML.equals(fileName) || (fileName.startsWith("ivy-") && fileName.endsWith(".xml")) ||
                fileName.endsWith(".ivy") ||
                fileName.endsWith("-" + IVY_XML);
    }
}