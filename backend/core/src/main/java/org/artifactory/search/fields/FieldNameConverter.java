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

package org.artifactory.search.fields;

import org.artifactory.api.rest.search.common.RestDateFieldName;
import org.artifactory.sapi.search.VfsDateFieldName;

/**
 * Date: 5/16/14 10:07 AM
 *
 * @author freds
 */
public abstract class FieldNameConverter {

    public static VfsDateFieldName fromRest(RestDateFieldName dateField) {
        switch (dateField) {
            case CREATED:
                return VfsDateFieldName.CREATED;
            case LAST_DOWNLOADED:
                return VfsDateFieldName.LAST_DOWNLOADED;
            case LAST_MODIFIED:
                return VfsDateFieldName.LAST_MODIFIED;
        }
        throw new IllegalArgumentException("Date Field " + dateField + " has not Query equivalent!");
    }

}
