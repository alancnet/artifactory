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

package org.artifactory.api.storage;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Represent the different storage units and their conversion to/from bytes Based on {@link
 * org.apache.wicket.util.lang.Bytes}
 *
 * @author Tomer Cohen
 */
public enum StorageUnit {
    /**
     * The kilobyte storage unit.
     */
    KB {
        @Override
        public double fromBytes(long size) {
            return size / UNITS_TO_NEXT_UNIT;
        }

        @Override
        public double toBytes(int size) {
            return size * UNITS_TO_NEXT_UNIT;
        }
    },
    /**
     * The megabyte storage unit.
     */
    MB {
        @Override
        public double fromBytes(long size) {
            return KB.fromBytes(size) / UNITS_TO_NEXT_UNIT;
        }

        @Override
        public double toBytes(int size) {
            return KB.toBytes(size) * UNITS_TO_NEXT_UNIT;
        }
    },
    /**
     * The gigabyte storage unit.
     */
    GB {
        @Override
        public double fromBytes(long size) {
            return MB.fromBytes(size) / UNITS_TO_NEXT_UNIT;
        }

        @Override
        public double toBytes(int size) {
            return MB.toBytes(size) * UNITS_TO_NEXT_UNIT;
        }
    },

    /**
     * The terabyte storage unit.
     */
    TB {
        @Override
        public double fromBytes(long size) {
            return GB.fromBytes(size) / UNITS_TO_NEXT_UNIT;
        }

        @Override
        public double toBytes(int size) {
            return GB.toBytes(size) * UNITS_TO_NEXT_UNIT;
        }
    };

    /**
     * The number of units to the transition to the next unit, i.e. there are 1024 bytes in a KB.
     */
    private static final double UNITS_TO_NEXT_UNIT = 1024.0;

    /**
     * Convert the number of bytes to the target storage unit.
     *
     * @param size The initial number in bytes.
     * @return The converted number of bytes in the target storage unit.
     */
    public abstract double fromBytes(long size);

    /**
     * Revert the number of the target storage unit to bytes.
     *
     * @param size The number of the target storage unit.
     * @return The converted number of target storage units back to bytes.
     */
    public abstract double toBytes(int size);

    /**
     * Convert the number of bytes to a human readable size, if the size is more than 1024 megabytes display the correct
     * number of gigabytes.
     *
     * @param size The size in bytes.
     * @return The size in human readable format.
     */
    public static String toReadableString(long size) {
        // if less than 1 byte, then simply return the bytes as it is
        if (size < UNITS_TO_NEXT_UNIT) {
            return size + " bytes";
        }
        DecimalFormat decimalFormat = createFormat();
        // convert to KB
        double readableSize = KB.fromBytes(size);
        // if less than 1 MB
        if (readableSize < UNITS_TO_NEXT_UNIT) {
            return decimalFormat.format(readableSize) + " KB";
        }
        // convert to MB
        readableSize = MB.fromBytes(size);
        // if less than 1 GB
        if (readableSize < UNITS_TO_NEXT_UNIT) {
            return decimalFormat.format(readableSize) + " MB";
        }
        readableSize = GB.fromBytes(size);
        return decimalFormat.format(readableSize) + " GB";
    }

    public static String format(double size) {
        return createFormat().format(size);
    }

    public static long fromReadableString(String humanReadableSize) {
        String number = humanReadableSize.replaceAll("([GgMmKkBb])", "");
        double d = Double.parseDouble(number);
        long l = Math.round(d * 1024 * 1024 * 1024L);
        int unitLength = humanReadableSize.length() - number.length();
        int unitIndex = unitLength > 0 ? humanReadableSize.length() - unitLength : 0;
        switch (humanReadableSize.charAt(unitIndex)) {
            default:
                l /= 1024;
            case 'K':
            case 'k':
                l /= 1024;
            case 'M':
            case 'm':
                l /= 1024;
            case 'G':
            case 'g':
                return l;
        }
    }

    private static DecimalFormat createFormat() {
        DecimalFormat decimalFormat = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.ENGLISH));
        decimalFormat.setMinimumFractionDigits(2);
        return decimalFormat;
    }
}
