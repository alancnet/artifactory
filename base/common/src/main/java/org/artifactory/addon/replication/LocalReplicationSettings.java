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

import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.repo.RepoPath;

/**
 * @author Noam Y. Tenne
 */
public class LocalReplicationSettings extends ReplicationBaseSettings {

    private final String username;
    private final String password;
    private final ProxyDescriptor proxyDescriptor;

    public LocalReplicationSettings(LocalReplicationDescriptor descriptor) {
        this(descriptor.getRepoPath(), descriptor.getUrl(), descriptor.getProxy(), descriptor.getSocketTimeoutMillis(),
                descriptor.getUsername(), descriptor.getPassword(), descriptor.isSyncDeletes(),
                descriptor.isSyncProperties());
    }

    /**
     * <B>NOTE<B>: Try to refrain from using this constructor directly and use the builder instead
     */
    public LocalReplicationSettings(RepoPath repoPath, String url, ProxyDescriptor proxyDescriptor,
            int socketTimeoutMillis, String username, String password, boolean deleteExisting,
            boolean includeProperties) {
        super(repoPath, deleteExisting, includeProperties, url, socketTimeoutMillis);
        this.proxyDescriptor = proxyDescriptor;
        this.username = username;
        this.password = password;
    }

    public ProxyDescriptor getProxyDescriptor() {
        return proxyDescriptor;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LocalReplicationSettings)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        LocalReplicationSettings that = (LocalReplicationSettings) o;

        if (password != null ? !password.equals(that.password) : that.password != null) {
            return false;
        }
        if (proxyDescriptor != null ? !proxyDescriptor.equals(that.proxyDescriptor) : that.proxyDescriptor != null) {
            return false;
        }
        if (!username.equals(that.username)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (proxyDescriptor != null ? proxyDescriptor.hashCode() : 0);
        return result;
    }
}
