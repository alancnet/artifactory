/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.api.rest.search.common;

/**
 * Date: 5/11/14 3:29 PM
 *
 * @author freds
 */
public enum  RestDateFieldName {
    LAST_MODIFIED("lastModified"), CREATED("created"), LAST_DOWNLOADED("lastDownloaded");

    public String fieldName;

    RestDateFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public static RestDateFieldName byFieldName(String fieldName) {
        for (RestDateFieldName dateFieldName : values()) {
            if (dateFieldName.fieldName.equals(fieldName)) {
                return dateFieldName;
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return fieldName;
    }
}
