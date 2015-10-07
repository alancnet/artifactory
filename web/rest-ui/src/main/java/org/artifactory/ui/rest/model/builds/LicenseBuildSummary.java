package org.artifactory.ui.rest.model.builds;

/**
 * @author Chen Keinan
 */
public class LicenseBuildSummary {

    private Integer approved;
    private Integer notFound;
    private Integer neutral;
    private Integer notApproved;
    private Integer unknown;

    public LicenseBuildSummary() {
    }

    public LicenseBuildSummary(int unknown, int approved, int neutral, int notApproved, int notFound) {
        this.unknown = unknown;
        this.approved = approved;
        this.neutral = neutral;
        this.notApproved = notApproved;
        this.notFound = notFound;
    }

    public Integer getApproved() {
        return approved;
    }

    public void setApproved(Integer approved) {
        this.approved = approved;
    }

    public Integer getNotFound() {
        return notFound;
    }

    public void setNotFound(Integer notFound) {
        this.notFound = notFound;
    }

    public Integer getNeutral() {
        return neutral;
    }

    public void setNeutral(Integer neutral) {
        this.neutral = neutral;
    }

    public Integer getNotApproved() {
        return notApproved;
    }

    public void setNotApproved(Integer notApproved) {
        this.notApproved = notApproved;
    }

    public Integer getUnknown() {
        return unknown;
    }

    public void setUnknown(Integer unknown) {
        this.unknown = unknown;
    }
}
