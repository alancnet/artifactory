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

import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.SaltedPassword;
import org.artifactory.security.UserInfo;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the UserInfoBuilder.
 *
 * @author Yossi Shaul
 */
@Test
public class UserInfoBuilderTest {

    public void minimalBuild() {
        UserInfoBuilder builder = new UserInfoBuilder("yossis");
        UserInfo user = builder.build();
        assertEquals(user.getUsername(), "yossis");
        assertEquals(user.getPassword(), MutableUserInfo.INVALID_PASSWORD);
        assertNotNull(user.getGroups());
        assertTrue(user.getGroups().isEmpty());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertFalse(user.isTransientUser());
        assertFalse(user.isAdmin());
        assertFalse(user.isAnonymous());
        assertTrue(user.isCredentialsNonExpired());
        assertFalse(user.isUpdatableProfile());
        assertTrue(user.isEnabled());
        assertEquals(user.getEmail(), "");
        assertNull(user.getGenPasswordKey());
        assertNull(user.getPrivateKey());
        assertNull(user.getPublicKey());
        assertNull(user.getPublicKey());
        assertNull(user.getLastLoginClientIp());
        assertEquals(user.getLastLoginTimeMillis(), 0);
        assertNull(user.getLastAccessClientIp());
        assertEquals(user.getLastAccessTimeMillis(), 0);
        assertNotNull(user.getSalt());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void noUsernameBuild() {
        UserInfoBuilder builder = new UserInfoBuilder(null);
        builder.build();
    }

    public void emailBuild() {
        UserInfoBuilder builder = new UserInfoBuilder("yossis");
        builder.email("yossis@test.org").password(new SaltedPassword("secret", "SALT"));
        UserInfo user = builder.build();
        assertEquals(user.getEmail(), "yossis@test.org");
        assertEquals(user.getPassword(), "secret");
    }

    public void saltBuild() {
        UserInfoBuilder builder = new UserInfoBuilder("yossis");
        builder.email("yossis@test.org").password(new SaltedPassword("secret", "SALT"));
        UserInfo user = builder.build();
        assertEquals(user.getEmail(), "yossis@test.org");
        assertEquals(user.getPassword(), "secret");
        assertEquals(user.getSalt(), "SALT");
    }

}
