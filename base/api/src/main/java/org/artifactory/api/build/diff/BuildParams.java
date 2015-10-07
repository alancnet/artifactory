package org.artifactory.api.build.diff;

/**
 * @author Chen Keinan
 */
public class BuildParams {

    private String currBuildNum;
    private String buildName;
    private String comperedBuildNum;
    private Long currBuildDate;
    private Long comperedBuildDate;
    private String buildModuleId;
    boolean allArtifact = false;
    boolean allDependencies = false;
    boolean isEnvProps = false;
    boolean excludeInternalDependencies = false;
    private String orderBy;
    private String direction;
    private String groupBy;
    private String offset;
    private String limit;

    public BuildParams(String moduleId, String buildNumber, String comparedBuildNum,
                       String comparedDate, String buildStarted, String buildName) {
        this.currBuildNum = buildNumber;
        this.comperedBuildNum = comparedBuildNum;
        if (buildStarted != null) {
            this.currBuildDate = Long.parseLong(buildStarted);
        }
        if (comparedBuildNum != null) {
            this.comperedBuildDate = Long.parseLong(comparedDate);
        }
        this.buildModuleId = moduleId;
        this.buildName = buildName;
    }

    public String getCurrBuildNum() {
        return currBuildNum;
    }

    public void setCurrBuildNum(String currBuildNum) {
        this.currBuildNum = currBuildNum;
    }

    public String getComperedBuildNum() {
        return comperedBuildNum;
    }

    public void setComperedBuildNum(String comperedBuildNum) {
        this.comperedBuildNum = comperedBuildNum;
    }

    public Long getCurrBuildDate() {
        return currBuildDate;
    }

    public void setCurrBuildDate(Long currBuildDate) {
        this.currBuildDate = currBuildDate;
    }

    public Long getComperedBuildDate() {
        return comperedBuildDate;
    }

    public void setComperedBuildDate(Long comperedBuildDate) {
        this.comperedBuildDate = comperedBuildDate;
    }

    public String getBuildModuleId() {
        return buildModuleId;
    }

    public void setBuildModuleId(String buildModuleId) {
        this.buildModuleId = buildModuleId;
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public boolean isAllArtifact() {
        return allArtifact;
    }

    public void setAllArtifact(boolean allArtifact) {
        this.allArtifact = allArtifact;
    }

    public boolean isAllDependencies() {
        return allDependencies;
    }

    public void setAllDependencies(boolean allDependencies) {
        this.allDependencies = allDependencies;
    }

    public boolean isEnvProps() {
        return isEnvProps;
    }

    public void setIsEnv(boolean isEnv) {
        this.isEnvProps = isEnv;
    }

    public boolean isExcludeInternalDependencies() {
        return excludeInternalDependencies;
    }

    public void setExcludeInternalDependencies(boolean excludeInternalDependencies) {
        this.excludeInternalDependencies = excludeInternalDependencies;
    }

    public void setIsEnvProps(boolean isEnvProps) {
        this.isEnvProps = isEnvProps;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }
}
