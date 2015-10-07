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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.artifactory.model.xstream.security.GroupImpl;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.MutableGroupInfo;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the GroupInfo class.
 *
 * @author Yossi Shaul
 */
@Test
public class GroupInfoTest {

    public void defaultConstructor() {
        GroupInfo info = new GroupImpl();
        Assert.assertNull(info.getGroupName(), "Group name should be null by default");
        Assert.assertNull(info.getDescription(), "Group description should be null by default");
        Assert.assertFalse(info.isNewUserDefault(), "Group should not be added by default");
    }

    public void copyConstructor() {
        MutableGroupInfo orig = new GroupImpl("name", "bla bla", false);
        GroupInfo copy = new GroupImpl(orig);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(orig, copy), "Orig and copy differ");

        orig.setNewUserDefault(true);
        copy = new GroupImpl(orig);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(orig, copy), "Orig and copy differ");
    }

}
