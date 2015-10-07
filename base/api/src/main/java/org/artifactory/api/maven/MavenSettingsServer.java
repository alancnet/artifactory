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

package org.artifactory.api.maven;

import org.apache.commons.lang.StringUtils;

/**
 * Contains information of a maven settings server
 *
 * @author Noam Y. Tenne
 */
public class MavenSettingsServer implements MavenSetting {

    private String id;
    private String username;
    private String password;

    /**
     * @param id       Server ID
     * @param username Username for server access
     * @param password Password for server access
     */
    public MavenSettingsServer(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the server ID
     *
     * @return Server ID
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Returns the server access username
     *
     * @return Server username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the server access password
     *
     * @return Server password
     */
    public String getPassword() {
        return password;
    }

    @Override
    public void isValid() {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("Setting data is not valid");
        }
    }
}
