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

package org.artifactory.storage.fs.service;

import org.artifactory.sapi.common.Lock;

import java.io.InputStream;

/**
 * Manages config files stored in Artifactory.
 *
 * @author Yossi Shaul
 */
public interface ConfigsService {

    /**
     * @param name Name of the config
     * @return The config data if the specified config exists
     */
    String getConfig(String name);

    /**
     * @param name Name of the config
     * @return True if config with the specified name exists
     */
    boolean hasConfig(String name);

    /**
     * Adds a new config to the storage. This method will throw {@link IllegalStateException} if the config already
     * exist.
     *
     * @param name Unique name of the config
     * @param data The data to save
     */
    @Lock
    void addConfig(String name, String data);

    /**
     * Updates the data of an existing config. This method will throw {@link IllegalStateException} if the config
     * doesn't already exist.
     *
     * @param name The name of the config to update
     * @param data The new data to set
     */
    @Lock
    void updateConfig(String name, String data);

    /**
     * Adds new config or updates an existing one with the input data.
     *
     * @param name The name of the config to add or update
     * @param data The data to set for this config
     * @return True is new record was created, false the record was updated
     */
    @Lock
    boolean addOrUpdateConfig(String name, String data);

    @Lock
    boolean addOrUpdateConfig(String name, InputStream data, long length);

    InputStream getStreamConfig(String name);

    @Lock
    void deleteConfig(String name);
}
