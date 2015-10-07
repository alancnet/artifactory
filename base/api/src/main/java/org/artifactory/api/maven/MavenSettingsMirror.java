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

package org.artifactory.api.maven;

import org.apache.commons.lang.StringUtils;

/**
 * Contains information of a maven settings mirror
 *
 * @author Noam Tenne
 */
public class MavenSettingsMirror extends MavenSettingUnit {
    private String mirrorOf;

    /**
     * Default constructor
     *
     * @param id       Repository ID
     * @param name     Repository name
     * @param mirrorOf mirrorOf expression
     */
    public MavenSettingsMirror(String id, String name, String mirrorOf) {
        super(id, name);
        this.mirrorOf = mirrorOf;
        isValid();
    }

    /**
     * Returns the mirrorOf expression
     *
     * @return String - mirrorOf expression
     */
    public String getMirrorOf() {
        return mirrorOf;
    }

    @Override
    public void isValid() {
        if (StringUtils.isEmpty(mirrorOf)) {
            throw new IllegalArgumentException("Setting data is not valid");
        }
    }
}