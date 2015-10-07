package org.artifactory.ui.rest.model.builds;

/**
 * @author Gidi Shabat
 */
public class BuildCoordinate {
    private String buildName;
    private String buildNumber;
    private long date;

    /**
     * Need the constructor for JSON mapping
     */
    public BuildCoordinate() {
    }

    public BuildCoordinate(String buildName, String buildNumber, long date) {
        this.buildName = buildName;
        this.buildNumber = buildNumber;
        this.date = date;
    }

    public BuildCoordinate(String buildName) {
        this.buildName = buildName;
    }

    public String getBuildName() {
        return buildName;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public long getDate() {
        return date;
    }
}
