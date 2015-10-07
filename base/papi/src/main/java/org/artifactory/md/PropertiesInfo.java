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

import org.artifactory.common.Info;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Date: 8/1/11
 * Time: 7:06 PM
 *
 * @author Fred Simon
 */
public interface PropertiesInfo extends Info {
    String ROOT = "properties";

    int size();

    @Nullable
    Set<String> get(@Nonnull String key);

    Collection<String> values();

    Set<Map.Entry<String, String>> entries();

    Set<String> keySet();

    boolean isEmpty();

    boolean containsKey(String key);

    /**
     * Returns the first value of the given key
     *
     * @param key Key of value
     * @return First found value of key; null if has no values at all
     */
    @Nullable
    String getFirst(@Nonnull String key);
}
