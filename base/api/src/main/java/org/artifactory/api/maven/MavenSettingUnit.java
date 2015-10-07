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
 * Base implementation of the maven setting
 *
 * @author Noam Tenne
 */
public abstract class MavenSettingUnit implements MavenSetting {
    protected String id;
    protected String name;

    /**
     * Default constructor
     *
     * @param id   Repository ID
     * @param name Repository name
     */
    public MavenSettingUnit(String id, String name) {
        this.id = id;
        this.name = name;
        isDataValid();
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Returns the repository name
     *
     * @return String - Repository name
     */
    public String getName() {
        return name;
    }

    private void isDataValid() {
        if ((StringUtils.isEmpty(id)) || (StringUtils.isEmpty(name))) {
            throw new IllegalArgumentException("Setting data is not valid");
        }
    }
}
