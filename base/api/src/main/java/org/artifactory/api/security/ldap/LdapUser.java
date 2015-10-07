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

package org.artifactory.api.security.ldap;

/**
 * Object representation of an LDAP user.
 *
 * @author Tomer Cohen
 */
public class LdapUser {

    private String uid;
    private String dn;

    public LdapUser(String uid, String dn) {
        this.dn = dn;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LdapUser)) {
            return false;
        }

        LdapUser ldapUser = (LdapUser) o;

        if (!uid.equals(ldapUser.uid)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("LdapUser");
        sb.append("{uid='").append(uid).append('\'');
        sb.append(", dn='").append(dn).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
