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

package org.artifactory.api.rest.build.diff;

import org.artifactory.api.build.diff.BuildsDiffArtifactModel;
import org.artifactory.api.build.diff.BuildsDiffDependencyModel;
import org.artifactory.api.build.diff.BuildsDiffPropertyModel;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A REST object to return for builds diff REST API
 *
 * @author Shay Yaakov
 */
public class BuildsDiff implements Serializable {

    public ArtifactsEntry artifacts = new ArtifactsEntry();
    public DependenciesEntry dependencies = new DependenciesEntry();
    public PropertiesEntry properties = new PropertiesEntry();

    public static class ArtifactsEntry {
        @XmlElement(name = "new")
        public List<BuildsDiffArtifactModel> newItems = new ArrayList<>();
        public List<BuildsDiffArtifactModel> updated = new ArrayList<>();
        public List<BuildsDiffArtifactModel> unchanged = new ArrayList<>();
        public List<BuildsDiffArtifactModel> removed = new ArrayList<>();
    }

    public static class DependenciesEntry {
        @XmlElement(name = "new")
        public List<BuildsDiffDependencyModel> newItems = new ArrayList<>();
        public List<BuildsDiffDependencyModel> updated = new ArrayList<>();
        public List<BuildsDiffDependencyModel> unchanged = new ArrayList<>();
        public List<BuildsDiffDependencyModel> removed = new ArrayList<>();
    }

    public static class PropertiesEntry {
        @XmlElement(name = "new")
        public List<BuildsDiffPropertyModel> newItems = new ArrayList<>();
        public List<BuildsDiffPropertyModel> updated = new ArrayList<>();
        public List<BuildsDiffPropertyModel> unchanged = new ArrayList<>();
        public List<BuildsDiffPropertyModel> removed = new ArrayList<>();
    }
}
