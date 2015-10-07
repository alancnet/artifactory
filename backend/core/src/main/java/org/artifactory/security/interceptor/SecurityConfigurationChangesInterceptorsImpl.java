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

package org.artifactory.security.interceptor;

import org.artifactory.repo.interceptor.Interceptors;
import org.artifactory.security.SecurityInfo;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.db.DbService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Yossi Shaul
 */
@Service
@Reloadable(beanClass = SecurityConfigurationChangesInterceptors.class, initAfter = DbService.class)
public class SecurityConfigurationChangesInterceptorsImpl extends Interceptors<SecurityConfigurationChangesInterceptor>
        implements SecurityConfigurationChangesInterceptors {

    @Override
    public void onUserAdd(String user) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onUserAdd(user);
        }
    }

    @Override
    public void onUserDelete(String user) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onUserDelete(user);
        }
    }

    @Override
    public void onAddUsersToGroup(String groupName, List<String> usernames) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onAddUsersToGroup(groupName, usernames);
        }
    }

    @Override
    public void onRemoveUsersFromGroup(String groupName, List<String> usernames) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onRemoveUsersFromGroup(groupName, usernames);
        }
    }

    @Override
    public void onGroupAdd(String group) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onGroupAdd(group);
        }
    }

    @Override
    public void onGroupDelete(String group) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onGroupDelete(group);
        }
    }

    @Override
    public void onPermissionsAdd() {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onPermissionsAdd();
        }
    }

    @Override
    public void onPermissionsUpdate() {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onPermissionsUpdate();
        }
    }

    @Override
    public void onPermissionsDelete() {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onPermissionsDelete();
        }
    }

    @Override
    public void onBeforeSecurityImport(SecurityInfo securityInfo) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onBeforeSecurityImport(securityInfo);
        }
    }
}