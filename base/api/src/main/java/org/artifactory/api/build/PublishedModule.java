package org.artifactory.api.build;

/**
 * @author Chen Keinan.
 */
public class PublishedModule {

    private String id;

    private String numOfArtifact;

    private String numOfDependencies;

    public String getNumOfArtifact() {
        return numOfArtifact;
    }

    public void setNumOfArtifact(String numOfArtifact) {
        this.numOfArtifact = numOfArtifact;
    }

    public String getNumOfDependencies() {
        return numOfDependencies;
    }

    public void setNumOfDependencies(String numOfDependencies) {
        this.numOfDependencies = numOfDependencies;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
