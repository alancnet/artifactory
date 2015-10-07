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

package org.artifactory.addon.nuget;

import org.artifactory.descriptor.property.Property;
import org.artifactory.md.PropertiesInfo;
import org.codehaus.plexus.util.StringUtils;

/**
 * Date: 12/6/12
 * Time: 6:21 PM
 *
 * @author freds
 */
public enum NuGetProperties {
    Id, Version, Digest,
    Title, Authors, Owners, Summary, Description,
    Copyright, RequireLicenseAcceptance, ReleaseNotes,
    ProjectUrl, LicenseUrl, IconUrl, Tags,
    Dependency;

    public static final String NUGET_PREFIX = "nuget";

    public String nodePropertyName() {
        return NUGET_PREFIX + "." + nuspecXmlTagName();
    }

    public String nuspecXmlTagName() {
        return StringUtils.lowercaseFirstLetter(name());
    }

    public Property getProperty() {
        return new Property(nodePropertyName());
    }

    public String extract(PropertiesInfo props) {
        return props.getFirst(nodePropertyName());
    }
}
