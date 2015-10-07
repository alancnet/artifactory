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

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.schedule.ArtifactoryHomeTaskTestStub;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.createMock;

/**
 * Test the password encoding logic with regard to Salt encoding
 *
 * @author Gidi Shabat
 */
public class SaltTest {

    private SecurityServiceImpl securityService;
    private UserGroupStoreService userGroupStoreService;

    @BeforeClass
    public void initArtifactoryRoles() {
        userGroupStoreService = createMock(UserGroupStoreService.class);
        ArtifactoryHome.bind(new ArtifactoryHomeTaskTestStub());
    }

    @BeforeMethod
    public void setUp() {
        securityService = new SecurityServiceImpl();
        ReflectionTestUtils.setField(securityService, "userGroupStoreService", userGroupStoreService);
        ReflectionTestUtils.setField(securityService, "passwordEncoder", new Md5PasswordEncoder());
    }

    @Test
    public void saltValueTest() {
        String defaultSalt = securityService.getDefaultSalt();
        Assert.assertEquals(defaultSalt, ConstantValues.defaultSaltValue.getString(),
                "Fail to fetch default Salt value");
    }

    @Test
    public void saltedPasswordWitDefaultSaltGenerationTest() {
        SaltedPassword saltedPassword = securityService.generateSaltedPassword("password");
        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
        // Expect to use the default Salt
        String salt = ConstantValues.defaultSaltValue.getString();
        SaltedPassword expectedPassword = new SaltedPassword(encoder.encodePassword("password", salt), salt);
        Assert.assertEquals(saltedPassword, expectedPassword, "Fail to generate secured password with Salt");
    }

    @Test
    public void saltedPasswordGenerationWithNullSaltTest() {
        // Expect to use the default Salt
        SaltedPassword securedPassword = securityService.generateSaltedPassword("password", null);
        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
        String salt = null;
        SaltedPassword expectedPassword = new SaltedPassword(encoder.encodePassword("password", salt), salt);
        Assert.assertEquals(securedPassword, expectedPassword, "Fail to generate secured password with Salt");
    }

    @Test
    public void saltedPasswordGenerationWithExistingUserTest() {
        // Expect to use the user Salt
        SaltedPassword securedPassword = securityService.generateSaltedPassword("password", "SALT");
        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
        SaltedPassword expectedPassword = new SaltedPassword(encoder.encodePassword("password", "SALT"), "SALT");
        Assert.assertEquals(securedPassword, expectedPassword, "Fail to generate secured password with Salt");
    }
}
