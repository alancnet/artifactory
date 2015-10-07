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

package org.artifactory.api.rest.artifact;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * @author Noam Y. Tenne
 */
public class ItemPermissions implements Serializable {

    private String uri;
    private Principals principals;

    public ItemPermissions() {
    }

    public ItemPermissions(String uri, Principals principals) {
        this.uri = uri;
        this.principals = principals;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Principals getPrincipals() {
        return principals;
    }

    public void setPrincipals(Principals principals) {
        this.principals = principals;
    }

    public static class Principals implements Serializable {

        Map<String, Set<String>> users;
        Map<String, Set<String>> groups;

        public Principals() {
        }

        public Principals(Map<String, Set<String>> users, Map<String, Set<String>> groups) {
            this.users = users;
            this.groups = groups;
        }

        public Map<String, Set<String>> getUsers() {
            return users;
        }

        public void setUsers(Map<String, Set<String>> users) {
            this.users = users;
        }

        public Map<String, Set<String>> getGroups() {
            return groups;
        }

        public void setGroups(Map<String, Set<String>> groups) {
            this.groups = groups;
        }
    }
}
