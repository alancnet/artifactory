/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

/**
 * Salted password contains the password hashed with the salt and salt value.
 *
 * @author Gidi Shabat
 */
public class SaltedPassword {

    // this instance represents an invalid password state and used when a user has no internal password (e.g., ldap user)
    public static final SaltedPassword INVALID_PASSWORD = new SaltedPassword(MutableUserInfo.INVALID_PASSWORD, "");

    private final String password;
    private final String salt;

    public SaltedPassword(String password, String salt) {
        this.password = password;
        this.salt = salt;
    }

    public String getPassword() {
        return password;
    }

    public String getSalt() {
        return salt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SaltedPassword saltedPassword1 = (SaltedPassword) o;

        if (password != null ? !password.equals(saltedPassword1.password) : saltedPassword1.password != null) {
            return false;
        }
        if (salt != null ? !salt.equals(saltedPassword1.salt) : saltedPassword1.salt != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = password != null ? password.hashCode() : 0;
        result = 31 * result + (salt != null ? salt.hashCode() : 0);
        return result;
    }

}
