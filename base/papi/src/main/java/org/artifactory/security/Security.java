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

package org.artifactory.security;

import org.artifactory.repo.RepoPath;
import org.artifactory.security.groups.CrowdGroupsSettings;
import org.artifactory.security.groups.LdapGroupsSettings;
import org.artifactory.security.groups.RealmAwareGroupsSettings;

/**
 * These are the usage of security data and logged in user methods.
 */
public interface Security {

    /**
     * @return True if the current user can update her profile.
     */
    boolean isUpdatableProfile();

    /**
     * @return True if anonymous access is allowed.
     */
    boolean isAnonAccessEnabled();

    /**
     * @return True if the current user can read the specified path.
     */
    boolean canRead(RepoPath path);

    /**
     * @return True if the current user can annotate the specified path.
     */
    boolean canAnnotate(RepoPath repoPath);

    /**
     * @return True if the current user can delete the specified path.
     */
    boolean canDelete(RepoPath path);

    /**
     * @return True if the current user can deploy to the specified path.
     */
    boolean canDeploy(RepoPath path);

    /**
     * @return True if the current user has admin permissions on a target info that includes this path..
     * @deprecated Use {@link #canManage()} instead
     */
    @Deprecated
    boolean canAdmin(RepoPath path);

    /**
     * @return True if the current user has manage permissions on a target info that includes this path..
     */
    boolean canManage(RepoPath path);

    /**
     * @return The current logged-in user name.
     * @since 2.3.3
     */
    String getCurrentUsername();

    /**
     * The current logged in-user name.
     *
     * @return The current logged in-user name
     * @deprecated Use  {@link #getCurrentUsername()} instead
     */
    @Deprecated
    String currentUsername();


    /**
     * The group names for the current logged-in user.
     *
     * @return A list of group names associated with the current user.
     * @since 2.3.3
     */
    String[] getCurrentUserGroupNames();

    /**
     * Retrieves the groups of the current user according to the given realm settings
     * Available realms are {@link LdapGroupsSettings} and {@link CrowdGroupsSettings}
     *
     * @param settings Realm specific settings
     * @return A list of group names associated with the current user with the relevant realm.
     * @since 4.1.0
     */
    String[] getCurrentUserGroupNames(RealmAwareGroupsSettings settings);

    /**
     * @return True if the current is a system administrator.
     */
    boolean isAdmin();

    /**
     * @return True if the current user is a anonymous.
     */
    boolean isAnonymous();

    /**
     * @return True if a user (anonymous and system are also users) is logged in.
     */
    boolean isAuthenticated();

    /**
     * @return The encrypted password of the current user
     */
    String getEncryptedPassword();

    /**
     * @return The encrypted password of the current user properly escaped for inclusion in xml settings
     */
    String getEscapedEncryptedPassword();

    /**
     * @return The current logged in user, the anonymous user or null if no authentication details present.
     */
    User currentUser();

    /**
     * Accessible only if current user is an admin.
     *
     * @return The user with the given username if exists or null.
     * @throws SecurityException if the current user is not an admin user
     */
    User findUser(String username);

    /**
     * Update the user with the same username as the user.getUsername() passed.<br/>
     * NOTE: Only updatable fields will be updated.<br/>
     * The user object cannot be null, and user.getUsername() should represent an existing non-anonymous or system user.
     * Here are the fields that will be updated and the conditions:<br/><ul>
     * <li>user.getEmail() will be updated if not null</li>
     * <li>user.isAdmin() will be updated</li>
     * <li>user.isEnabled() will be updated</li>
     * <li>user.isUpdatableProfile() will be updated</li>
     * <li>user.getPrivateKey() will be updated if not null</li>
     * <li>user.getPublicKey() will be updated if not null</li>
     * <li>user.isTransientUser() will be updated</li>
     * <li>user.getGroups() will be updated if not null and all group name exists</li>
     * <li>user.getBintrayAuth() will be updated if not null</li>
     * </ul>
     *
     * @param user the user with all the fields to update
     * @return the new updated user data
     * @throws SecurityException                        if the current user is not an admin user, if the user passed is the anonymous user,
     *                                                  if one of the group passed does not exists
     * @throws org.artifactory.storage.StorageException if the user could not updated
     */
    void updateUser(User user);
}
