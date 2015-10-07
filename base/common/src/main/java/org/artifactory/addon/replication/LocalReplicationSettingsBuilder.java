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

package org.artifactory.addon.replication;

import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.repo.RepoPath;

/**
 * @author Noam Y. Tenne
 */
public class LocalReplicationSettingsBuilder {

    private final RepoPath repoPath;
    private final String url;
    private ProxyDescriptor proxyDescriptor;
    private int socketTimeoutMillis;
    private String username;
    private String password;
    private boolean deleteExisting = false;
    private boolean includeProperties = false;

    public LocalReplicationSettingsBuilder(RepoPath repoPath, String url) {
        this.repoPath = repoPath;
        this.url = url;
    }

    public LocalReplicationSettingsBuilder proxyDescriptor(ProxyDescriptor proxyDescriptor) {
        this.proxyDescriptor = proxyDescriptor;
        return this;
    }

    public LocalReplicationSettingsBuilder socketTimeoutMillis(int socketTimeoutMillis) {
        this.socketTimeoutMillis = socketTimeoutMillis;
        return this;
    }

    public LocalReplicationSettingsBuilder username(String username) {
        this.username = username;
        return this;
    }

    public LocalReplicationSettingsBuilder password(String password) {
        this.password = password;
        return this;
    }

    public LocalReplicationSettingsBuilder deleteExisting(boolean deleteExisting) {
        this.deleteExisting = deleteExisting;
        return this;
    }

    public LocalReplicationSettingsBuilder includeProperties(boolean includeProperties) {
        this.includeProperties = includeProperties;
        return this;
    }

    public LocalReplicationSettings build() {
        return new LocalReplicationSettings(repoPath, url, proxyDescriptor, socketTimeoutMillis, username, password,
                deleteExisting, includeProperties);
    }
}
