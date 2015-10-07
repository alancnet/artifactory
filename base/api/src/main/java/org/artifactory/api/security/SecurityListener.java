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

package org.artifactory.api.security;

/**
 * A listener for security service events
 * <p/>
 * TODO [YS]: this interface should be merged with {@link org.artifactory.security.interceptor.SecurityConfigurationChangesInterceptors}
 * TODO [FS]: The security conf change listener has no on updates events
 *
 * @author Yoav Landman
 */
public interface SecurityListener extends Comparable<SecurityListener> {

    /**
     * Called whenever a complete security caches cleanup should be done.
     */
    void onClearSecurity();

    /**
     * Called when a user is deleted from Artifactory.
     *
     * @param username The deleted username
     */
    void onUserDelete(String username);

    /**
     * Called when a user is updated in Artifactory.
     *
     * @param username The username of the updated user
     */
    void onUserUpdate(String username);
}
