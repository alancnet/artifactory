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

import java.text.DecimalFormat;

/**
 * Utility for frequently used number formats.
 *
 * @author Yossi Shaul
 */
public abstract class NumberFormatter {

    /**
     * Returns a formatted long value according to the pattern "#,###".<p/>
     * For example:
     * <pre>
     * formatLong(100) = "100"
     * formatLong(1000) = "1,000"
     * formatLong(10000) = "10,000"
     * formatLong(1000000) = "1,000,000"
     * </pre>
     *
     * @param val The value to format
     * @return A formatted long value according to the pattern "#,###"
     */
    public static String formatLong(long val) {
        return new DecimalFormat("#,###").format(val);
    }

    /**
     * Returns formatted percentage value for the given fraction.<p/>
     * For example:
     * <pre>
     * formatPercentage(0.1) = "10%"
     * formatPercentage(0.55558) = "55.56%"
     * formatPercentage(2.3) = "230%"
     * </pre>
     *
     * @param fraction The fraction to turn into percentage format
     * @return A formatted percentage value for the given fraction
     */
    public static String formatPercentage(double fraction) {
        if (Double.isNaN(fraction)) {
            return "N/A";
        }
        return new DecimalFormat("###.##%").format(fraction);
    }
}
