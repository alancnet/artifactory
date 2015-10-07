package org.artifactory.ui.rest.model.artifacts.search.gavcsearch;

import org.artifactory.ui.rest.model.artifacts.search.BaseSearch;

/**
 * @author Chen Keinan
 */
public class GavcSearch extends BaseSearch {

    private String groupID;
    private String artifactID;
    private String version;
    private String classifier;

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getArtifactID() {
        return artifactID;
    }

    public void setArtifactID(String artifactID) {
        this.artifactID = artifactID;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }
}
