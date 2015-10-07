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

package org.artifactory.api.rest.search.result;

import java.util.List;

/**
 * A wrapper class for a JSON object representation of Artifactory's version, revision, and a list of enabled addons.
 *
 * @author Tomer Cohen
 */
public class VersionRestResult {

    public String version;
    public String revision;
    public List<String> addons;
    public String license;

    public VersionRestResult(String version, String revision, List<String> addons, String license) {
        this.version = version;
        this.revision = revision;
        this.addons = addons;
        this.license = license;
    }

    /**
     * Constructor used by the JSON parser
     */
    private VersionRestResult() {

    }

}
