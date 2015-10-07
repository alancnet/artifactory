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

package org.artifactory.util;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link NumberFormatter}
 *
 * @author Yossi Shaul
 */
@Test
public class NumberFormatterTest {

    public void formatLong() {
        assertEquals(NumberFormatter.formatLong(0), "0");
        assertEquals(NumberFormatter.formatLong(1), "1");
        assertEquals(NumberFormatter.formatLong(100), "100");
        assertEquals(NumberFormatter.formatLong(1_000), "1,000");
        assertEquals(NumberFormatter.formatLong(10_000), "10,000");
        assertEquals(NumberFormatter.formatLong(1_000_000), "1,000,000");
        assertEquals(NumberFormatter.formatLong(1_000_001_000), "1,000,001,000");
    }

    public void formatPercentage() {
        assertEquals(NumberFormatter.formatPercentage(0), "0%");
        assertEquals(NumberFormatter.formatPercentage(0.0), "0%");
        assertEquals(NumberFormatter.formatPercentage(1), "100%");
        assertEquals(NumberFormatter.formatPercentage(0.5555), "55.55%");
        if (System.getProperty("java.version").startsWith("1.8")) {
            assertEquals(NumberFormatter.formatPercentage(0.55555), "55.55%");
        } else {
            assertEquals(NumberFormatter.formatPercentage(0.55555), "55.56%");
        }
        assertEquals(NumberFormatter.formatPercentage(0.55558), "55.56%");
        assertEquals(NumberFormatter.formatPercentage(0.55554), "55.55%");
        assertEquals(NumberFormatter.formatPercentage(30.55554), "3055.55%");
    }

}
