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

package org.artifactory.common.wicket.component.file.path;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.util.convert.IConverter;

import java.io.File;
import java.util.Locale;

public class PathAutoCompleteConverter implements IConverter {
    private PathHelper pathHelper;

    public PathAutoCompleteConverter(PathHelper pathHelper) {
        this.pathHelper = pathHelper;
    }

    @Override
    public Object convertToObject(String value, Locale locale) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return pathHelper.getAbsoluteFile(value);
    }

    @Override
    public String convertToString(Object value, Locale locale) {
        if (value == null) {
            return null;
        }

        File file;
        if (value instanceof String) {
            file = new File((String) value);
        } else {
            file = (File) value;
        }

        return pathHelper.getRelativePath(file);
    }
}
