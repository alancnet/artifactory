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

package org.artifactory.build;

import org.jfrog.build.api.BaseBuildFileBean;

import java.io.Serializable;

/**
 * A basic build run info holder
 *
 * @author Noam Y. Tenne
 */
abstract class BuildFile implements Serializable {

    private BaseBuildFileBean buildFile;

    protected BuildFile(BaseBuildFileBean buildFile) {
        this.buildFile = buildFile;
    }

    public String getType() {
        return buildFile.getType();
    }

    public String getSha1() {
        return buildFile.getSha1();
    }

    public String getMd5() {
        return buildFile.getMd5();
    }
}