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

package org.artifactory.security.ldap;

import org.artifactory.api.security.ldap.LdapUser;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.AbstractContextMapper;

/**
 * User mapper from ldap
 *
 * @author Tomer Cohen
 * @see GroupContextMapper
 */
public class UserContextMapper extends AbstractContextMapper {

    /**
     * Method to map user from ldap attributes
     *
     * @param ctx The ldap context
     * @return The mapped ldap user {@link org.artifactory.api.security.ldap.LdapUser}
     */
    @Override
    protected LdapUser doMapFromContext(DirContextOperations ctx) {
        String uidFromLdap = ctx.getStringAttribute("uid");
        return new LdapUser(uidFromLdap, ctx.getNameInNamespace());
    }
}
