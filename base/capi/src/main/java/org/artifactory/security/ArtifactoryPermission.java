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

package org.artifactory.security;

/**
 * @author Yoav Landman
 */
public enum ArtifactoryPermission {
    MANAGE(4, "m"), DELETE(3, "d"), ANNOTATE(2, "n"), DEPLOY(1, "w"), READ(0, "r");

    private final int mask;
    private final String string;

    ArtifactoryPermission(int bitPos, String string) {
        this.mask = 1 << bitPos;
        this.string = string;
    }

    public int getMask() {
        return mask;
    }

    public String getString() {
        return string;
    }
}
