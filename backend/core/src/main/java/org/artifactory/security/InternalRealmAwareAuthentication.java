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

import org.artifactory.security.db.DbAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @author Tomer Cohen
 */
public class InternalRealmAwareAuthentication extends RealmAwareUserPassAuthenticationToken {

    public InternalRealmAwareAuthentication(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public InternalRealmAwareAuthentication(Object principal, Object credentials,
            Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    @Override
    public String getRealm() {
        return DbAuthenticationProvider.INTERNAL_REALM;
    }
}
