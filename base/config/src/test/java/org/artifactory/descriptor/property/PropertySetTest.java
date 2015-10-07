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

package org.artifactory.descriptor.property;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for the PropertySet object
 *
 * @author Noam Tenne
 */
@Test
public class PropertySetTest {

    public void defaultConstructor() {
        PropertySet propertySet = new PropertySet();
        Assert.assertNull(propertySet.getName(), "Default property set name should be null.");
        Assert.assertTrue(propertySet.isVisible(), "Property set should be visible by default.");
        Assert.assertTrue(propertySet.getProperties().isEmpty(), "Property list should be empty be default.");
    }

    /**
     * Test equals method with 2 equal property set objects
     */
    public void testEqual() {
        PropertySet moo1 = new PropertySet();
        PropertySet moo2 = new PropertySet();
        moo1.setName("moo");
        moo2.setName("moo");

        Assert.assertTrue(moo1.equals(moo2), "Property sets should be equal.");
    }

    /**
     * Test equals method with 2 unequal property set objects
     */
    public void testUnequal() {
        PropertySet propertySetMoo = new PropertySet();
        propertySetMoo.setName("moo");

        PropertySet propertySetBoo = new PropertySet();
        propertySetBoo.setName("boo");

        Assert.assertFalse(propertySetMoo.equals(propertySetBoo), "Property sets should not be equal.");
    }
}
