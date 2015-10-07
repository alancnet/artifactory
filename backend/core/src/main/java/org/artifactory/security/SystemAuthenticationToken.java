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

import org.artifactory.api.security.SecurityService;
import org.artifactory.factory.InfoFactoryHolder;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.io.Serializable;

/**
 * An authentication object to be used when needing authentication details for system tasks
 *
 * @author Noam Y. Tenne
 */
public class SystemAuthenticationToken extends AbstractAuthenticationToken implements Serializable {

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal represented by this
     *                    authentication object.
     */
    public SystemAuthenticationToken() {
        super(SimpleUser.ADMIN_GAS);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        MutableUserInfo user = InfoFactoryHolder.get().createUser(SecurityService.USER_SYSTEM);
        user.setAdmin(true);
        return new SimpleUser(user);
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }
}
