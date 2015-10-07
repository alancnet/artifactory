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

package org.artifactory.api.archive;

/**
 * An object which defines the supported archiving types
 *
 * @author Shay Yaakov
 */
public enum ArchiveType {
    ZIP("zip"), TAR("tar"), TARGZ("tar.gz"), TGZ("tgz");

    private final String value;

    ArchiveType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static ArchiveType fromValue(String v) {
        for (ArchiveType c : ArchiveType.values()) {
            if (c.value.equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Archive type parameter must be one of: 'zip','tar','tar.gz','tgz'. You " +
                "sent: " + v);
    }
}
