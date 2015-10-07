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

import org.artifactory.api.rest.artifact.RestBaseStorageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Json object retuning the ArtifactSearchResource,GavcSearchResource, PropertySearchResource search results
 *
 * @author Eli Givoni
 */
public class InfoRestSearchResult {
    public List<RestBaseStorageInfo> results = new ArrayList<>();
}
