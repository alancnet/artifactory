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

package org.artifactory.util;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests {@link org.artifactory.util.Strings}.
 *
 * @author Yossi Shaul
 */
@Test
public class StringsTest {
    public void maskString() {
        assertEquals(Strings.mask("dogs"), "****");
    }

    public void maskNull() {
        assertEquals(Strings.mask(null), "");
    }

    public void maskEmpty() {
        assertEquals(Strings.mask(""), "");
    }

    public void maskKeyValue() {
        assertEquals(Strings.maskKeyValue("good=freedom"), "good=*******");
    }

    public void maskKeyValueNoEquals() {
        assertEquals(Strings.maskKeyValue("dogs"), "dogs");
    }

    public void maskKeyValueNull() {
        assertEquals(Strings.maskKeyValue(null), "");
    }

    public void maskKeyValueEmpty() {
        assertEquals(Strings.maskKeyValue(""), "");
    }
}
