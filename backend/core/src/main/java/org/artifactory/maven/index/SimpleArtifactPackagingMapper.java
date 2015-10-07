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

package org.artifactory.maven.index;

import org.apache.maven.index.artifact.ArtifactPackagingMapper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapper from maven packaging type to file extension.
 * <p/>
 * Based on org.apache.maven.index.artifact.DefaultArtifactPackagingMapper
 *
 * @author Yossi Shaul
 */
public class SimpleArtifactPackagingMapper implements ArtifactPackagingMapper {

    private static final Map<String, String> defaults;

    static {
        defaults = new HashMap<>();
        defaults.put("ejb-client", "jar");
        defaults.put("ejb", "jar");
        defaults.put("rar", "jar");
        defaults.put("par", "jar");
        defaults.put("maven-plugin", "jar");
        defaults.put("maven-archetype", "jar");
        defaults.put("plexus-application", "jar");
        defaults.put("eclipse-plugin", "jar");
        defaults.put("eclipse-feature", "jar");
        defaults.put("eclipse-application", "zip");
        defaults.put("java-source", "jar");
        defaults.put("javadoc", "jar");
        defaults.put("test-jar", "jar");
    }

    @Override
    public String getExtensionForPackaging(String packaging) {
        if (packaging == null) {
            return "jar";
        }

        if (defaults.containsKey(packaging)) {
            return defaults.get(packaging);
        } else {
            // default's to packaging name, ie. "jar", "war", "pom", etc.
            return packaging;
        }
    }

    @Override
    public void setPropertiesFile(File propertiesFile) {
        // support for  external configuration not implemented
    }
}