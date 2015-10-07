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

package org.artifactory.webapp.wicket.page.security.acl;

import org.artifactory.security.PermissionTargetInfo;

/**
 * @author Yoav Landman
 */
public enum CommonPathPattern {

    NONE("None", ""),
    ANY("Any", PermissionTargetInfo.ANY_PATH),
    SOURCES("Source artifacts", "**/*-sources.*"),
    SNAPSHOTS("Snapshot artifacts", "**/*-SNAPSHOT/**"),
    PACKAGES("Artifacts of package com.acme", "com/acme/**");

    private String displayName;
    private String pattern;

    CommonPathPattern(String displayName, String pattern) {
        this.displayName = displayName;
        this.pattern = pattern;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
