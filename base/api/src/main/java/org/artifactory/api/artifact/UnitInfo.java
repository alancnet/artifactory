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

package org.artifactory.api.artifact;

import org.artifactory.api.maven.MavenArtifactInfo;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.io.Serializable;

/**
 * A unit info representation.
 *
 * @author Tomer Cohen
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "artifactType")
@JsonSubTypes({@JsonSubTypes.Type(value = MavenArtifactInfo.class, name = "maven"),
        @JsonSubTypes.Type(value = DebianArtifactInfo.class, name = "debian"),
        @JsonSubTypes.Type(value = ArtifactInfo.class, name = "base")})
public interface UnitInfo extends Serializable {
    /**
     * Represents a value that is not set or not available (for some implementers it indicates an invalid state)
     */
    public static final String NA = "NA";

    /**
     * Returns whether this artifact is a Maven artifact or a Generic type artifact.
     *
     * @return Whether this artifact is a Maven artifact or a Generic type artifact.
     */
    boolean isMavenArtifact();

    /**
     * Returns the path of the artifact. If it is Maven it is automatically guessed by Artifactory, otherwise it is a
     * user-defined path.
     *
     * @return The path of the artifact
     */
    String getPath();

    /**
     * Checks whether this artifact has a valid path. If it is a Maven path it checks that the repo path is a valid
     * Maven 2 path, if it is generic it checks that it is not blank.
     *
     * @return True if the artifact is valid, false otherwise.
     */
    boolean isValid();

    void setPath(String path);
}
