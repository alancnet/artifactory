package org.artifactory.api.artifact;

import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Chen keinan
 */
@JsonTypeName("debian")
public class DebianArtifactInfo implements UnitInfo {

    private String artifactType = "debian";
    private String path;

    public DebianArtifactInfo() {
    }

    public DebianArtifactInfo(String path) {
        this.path = path;
    }

    @Override
    public boolean isMavenArtifact() {
        return false;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    public String getArtifactType() {
        return artifactType;
    }

    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }
}
