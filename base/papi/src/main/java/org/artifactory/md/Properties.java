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

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

/**
 * A map of stringified keys and values, used for storing arbitrary key-value metadata on repository items.
 *
 * @author Yoav Landman
 */
public interface Properties extends MutablePropertiesInfo {
    String MATRIX_PARAMS_SEP = ";";
    /**
     * A mandatory property is stored as key+=val
     */
    String MANDATORY_SUFFIX = "+";

    boolean putAll(Multimap<? extends String, ? extends String> multimap);

    Multiset<String> keys();

    /**
     * @return True if there is a property with a mandatory key
     * @see Properties#MANDATORY_SUFFIX
     */
    boolean hasMandatoryProperty();

    MatchResult matchQuery(Properties queryProperties);

    public enum MatchResult {
        MATCH,
        NO_MATCH,
        CONFLICT
    }
}