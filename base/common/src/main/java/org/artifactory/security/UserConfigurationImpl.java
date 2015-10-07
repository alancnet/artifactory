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

import java.util.Set;

/**
 * @author Noam Y. Tenne
 */
public class UserConfigurationImpl {

    private String name;
    private String email;
    private String password;
    private boolean admin;
    private boolean profileUpdatable = true;
    private boolean internalPasswordDisabled;
    private Set<String> groups;
    private String lastLoggedIn;
    private String realm;
    private boolean offlineMode;

    public UserConfigurationImpl() {
    }

    /**
     * @return The username
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isProfileUpdatable() {
        return profileUpdatable;
    }

    public void setProfileUpdatable(boolean profileUpdatable) {
        this.profileUpdatable = profileUpdatable;
    }

    public boolean isInternalPasswordDisabled() {
        return internalPasswordDisabled;
    }

    public void setInternalPasswordDisabled(boolean internalPasswordDisabled) {
        this.internalPasswordDisabled = internalPasswordDisabled;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public String getLastLoggedIn() {
        return lastLoggedIn;
    }

    public void setLastLoggedIn(String lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public boolean isOfflineMode() {
        return offlineMode;
    }

    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserConfigurationImpl)) {
            return false;
        }

        UserConfigurationImpl that = (UserConfigurationImpl) o;

        if (admin != that.admin) {
            return false;
        }
        if (internalPasswordDisabled != that.internalPasswordDisabled) {
            return false;
        }
        if (profileUpdatable != that.profileUpdatable) {
            return false;
        }
        if (email != null ? !email.equals(that.email) : that.email != null) {
            return false;
        }
        if (groups != null ? !groups.equals(that.groups) : that.groups != null) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }
        if (realm != null ? !realm.equals(that.realm) : that.realm != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + (admin ? 1 : 0);
        result = 31 * result + (profileUpdatable ? 1 : 0);
        result = 31 * result + (internalPasswordDisabled ? 1 : 0);
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        result = 31 * result + (realm != null ? realm.hashCode() : 0);
        return result;
    }
}
