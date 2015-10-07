/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.model.xstream.security;

import org.artifactory.security.SaltedPassword;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link UserImpl}
 *
 * @author Yossi Shaul
 */
@Test
public class UserImplTest {

    public void sameAuthContextForSameInstance() {
        UserImpl paul = new UserImpl("paul");
        paul.setPassword(new SaltedPassword("atreides", "000"));
        assertTrue(paul.hasSameAuthorizationContext(paul));
    }

    public void authContextDifferentIfPasswordIfDifferent() {
        UserImpl leto = new UserImpl("leto");
        leto.setPassword(new SaltedPassword("atreides", "000"));

        UserImpl letoWithNewPass = new UserImpl("leto");
        leto.setPassword(new SaltedPassword("tyrant", "000"));

        assertFalse(leto.hasSameAuthorizationContext(letoWithNewPass));
    }

    public void sameAuthContextUserWithNoPassword() {
        UserImpl paul = new UserImpl("paul");
        assertTrue(paul.hasSameAuthorizationContext(paul));
    }

}