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

package org.artifactory.api.rest.artifact;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Eli Givoni
 */
public abstract class RestBaseStorageInfo implements Serializable {
    @XmlElement(name = "uri")
    public String slf;
    public String repo;
    public String path;
    public String created;
    public String createdBy;
    public String lastModified;
    public String modifiedBy;
    public String lastUpdated;
    public Map<String, String[]> properties;
}
