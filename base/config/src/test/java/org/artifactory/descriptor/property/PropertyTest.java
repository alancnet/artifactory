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
 * Unit test for the Property object
 *
 * @author Noam Tenne
 */
@Test
public class PropertyTest {

    public void defaultConstructor() {
        Property property = new Property();
        Assert.assertNull(property.getName(), "Default property name should be null.");
        Assert.assertFalse(property.isClosedPredefinedValues(), "Property predefined values should not be closed by" +
                " default.");
        Assert.assertTrue(property.getPredefinedValues().isEmpty(), "Property predefined value list should be empty" +
                " by default.");
    }
}
