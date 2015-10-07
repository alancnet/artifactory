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

package org.artifactory.api.rest.replication;

import java.io.Serializable;

/**
 * @author Noam Y. Tenne
 */
public class ReplicationRequest implements Serializable {

    private boolean properties = true;
    private boolean delete = false;

    /**
     * Needed only for local replications
     */
    private String url;
    private String username;
    private String password;
    private String proxy;

    public ReplicationRequest(boolean properties, boolean delete, String url, String username, String password,
            String proxy) {
        this.properties = properties;
        this.delete = delete;
        this.url = url;
        this.username = username;
        this.password = password;
        this.proxy = proxy;
    }

    public ReplicationRequest() {
    }

    public boolean isProperties() {
        return properties;
    }

    public void setProperties(boolean properties) {
        this.properties = properties;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }
}
