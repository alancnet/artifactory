package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.builds;

import org.artifactory.build.BuildRun;
/**
 * @author Chen Keinan
 */
public class UsedBy {

    private String name;
    private String number;
    private String ciUrl;
    private String releaseStatus;
    private String moduleID;
    private String scope;
    private String started;

    public UsedBy(BuildRun dependencyBuild,String moduleID,String scope) {
        if (dependencyBuild != null){
            this.name = dependencyBuild.getName();
            this.number = dependencyBuild.getNumber();
             this.ciUrl = dependencyBuild.getCiUrl();
            this.releaseStatus = dependencyBuild.getReleaseStatus();
            this.moduleID = moduleID;
            this.scope = scope;
            this.started= new Long(dependencyBuild.getStartedDate().getTime()).toString();
        }
    }

    public UsedBy() {
    }


    public String getStarted() {
        return started;
    }

    public void setStarted(String started) {
        this.started = started;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCiUrl() {
        return ciUrl;
    }

    public void setCiUrl(String ciUrl) {
        this.ciUrl = ciUrl;
    }

    public String getReleaseStatus() {
        return releaseStatus;
    }

    public void setReleaseStatus(String releaseStatus) {
        this.releaseStatus = releaseStatus;
    }

    public String getModuleID() {
        return moduleID;
    }

    public void setModuleID(String moduleID) {
        this.moduleID = moduleID;
    }
}
