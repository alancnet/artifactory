package org.artifactory.build;

import org.artifactory.fs.FileInfo;
import org.jfrog.build.api.Artifact;

/**
 * @author Dan Feldman
 */
public class ArtifactoryBuildArtifact {

    private final FileInfo fileInfo;
    private final Artifact artifact;

    public ArtifactoryBuildArtifact(Artifact artifact, FileInfo fileInfo) {
        this.artifact = artifact;
        this.fileInfo = fileInfo;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArtifactoryBuildArtifact)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ArtifactoryBuildArtifact that = (ArtifactoryBuildArtifact) o;
        if (!artifact.equals(that.artifact)) {
            return false;
        }
        if (getFileInfo() != null && that.getFileInfo() != null) {
            return getFileInfo().isIdentical(that.getFileInfo());
        } else {
            return getFileInfo() != null || that.getFileInfo() != null;
        }
    }

    @Override
    public int hashCode() {
        int result = artifact != null ? artifact.hashCode() : 1;
        result = 31 * result + (getFileInfo() != null ? getFileInfo().hashCode() : 0);
        return result;
    }
}
