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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Date: 8/1/11
 * Time: 7:34 PM
 *
 * @author Fred Simon
 */
public interface MutablePropertiesInfo extends PropertiesInfo {
    boolean putAll(@Nullable String key, Iterable<? extends String> values);

    @Nullable
    Set<? extends String> replaceValues(@Nonnull String key, Iterable<? extends String> values);

    void clear();

    Set<String> removeAll(@Nullable Object key);

    boolean put(String key, String value);
}
