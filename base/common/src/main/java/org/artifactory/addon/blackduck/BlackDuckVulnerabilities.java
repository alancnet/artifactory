package org.artifactory.addon.blackduck;

/**
 * @author chen Keinan
 */
public class BlackDuckVulnerabilities {

    private String artifactID;
    private String name;
    private String severity;
    private String description;

    public BlackDuckVulnerabilities(String artifactID, String name, String severity, String description) {
        this.artifactID = artifactID;
        this.name = name;
        this.severity = severity;
        this.description = description;
    }

    public String getArtifactID() {
        return artifactID;
    }

    public void setArtifactID(String artifactID) {
        this.artifactID = artifactID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
