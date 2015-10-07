package org.artifactory.ui.rest.model.artifacts.search;

import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.DeleteArtifact;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class DeleteArtifactsModel extends BaseModel {
    private List<DeleteArtifact> artifacts;

    public List<DeleteArtifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<DeleteArtifact> artifacts) {
        this.artifacts = artifacts;
    }
}
