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

import org.artifactory.factory.InfoFactoryHolder;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.io.Serializable;

/**
 * An authentication object to be used when needing authentication details for anonymous tasks
 *
 * @author Fred Simon
 */
public class AnonymousAuthenticationToken extends AbstractAuthenticationToken implements Serializable {

    /**
     * Creates a token for the anonymous user
     */
    public AnonymousAuthenticationToken() {
        super(SimpleUser.USER_GAS);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return new SimpleUser(InfoFactoryHolder.get().createUser(UserInfo.ANONYMOUS));
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }
}
