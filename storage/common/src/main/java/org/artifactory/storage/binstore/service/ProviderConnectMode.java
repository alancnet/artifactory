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

package org.artifactory.storage.binstore.service;

import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Date: 12/16/12
 * Time: 5:21 PM
 *
 * @author freds
 */
public enum ProviderConnectMode {
    PASS_THROUGH("passThrough"), COPY_ON_READ("copyOnRead"), COPY_FIRST("copyFirst"), MOVE("move");

    private ProviderConnectMode(String propName) {
        this.propName = propName;
    }

    public final String propName;

    private static final Map<String, ProviderConnectMode> CONNECT_MODES = Maps.newHashMapWithExpectedSize(4);

    @Nonnull
    public static ProviderConnectMode getConnectMode(String propVal) {
        if (CONNECT_MODES.isEmpty()) {
            ProviderConnectMode[] values = ProviderConnectMode.values();
            for (ProviderConnectMode value : values) {
                CONNECT_MODES.put(value.propName, value);
            }
        }
        ProviderConnectMode mode = CONNECT_MODES.get(propVal);
        if (mode == null) {
            throw new IllegalArgumentException("Connect mode value " + propVal + " is invalid!" +
                    "Choose one of " + CONNECT_MODES.keySet());
        }
        return mode;
    }
}
