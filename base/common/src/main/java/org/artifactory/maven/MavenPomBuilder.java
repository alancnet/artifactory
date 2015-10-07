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

package org.artifactory.maven;

import org.apache.maven.model.Model;
import org.artifactory.api.util.Builder;

/**
 * Builder for maven {@link Model}.
 *
 * @author Yossi Shaul
 */
public class MavenPomBuilder implements Builder<Model> {
    private String groupId;
    private String artifactId;
    private String version;
    private String packaging;

    @Override
    public Model build() {
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId(groupId);
        model.setArtifactId(artifactId);
        model.setVersion(version);
        model.setPackaging(packaging);
        model.setDescription("Artifactory auto generated POM");
        return model;
    }

    public MavenPomBuilder groupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public MavenPomBuilder artifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public MavenPomBuilder version(String version) {
        this.version = version;
        return this;
    }

    public MavenPomBuilder packaging(String packaging) {
        this.packaging = packaging;
        return this;
    }
}
