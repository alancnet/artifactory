package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.builds;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.build.BuildRun;

/**
 * @author Chen Keinan
 */
public class ProduceBy {

    private String name;
    private String number;
    private long started;
    private String startedString;
    private String ciUrl;
    private String releaseStatus;
    private String moduleID;

    public ProduceBy(BuildRun artifactBuild, String module) {
        if (artifactBuild != null) {
            this.name = artifactBuild.getName();
            this.number = artifactBuild.getNumber();
            this.started = artifactBuild.getStartedDate().getTime();
            this.startedString = ContextHelper.get().getCentralConfig().format(this.started);
            this.ciUrl = artifactBuild.getCiUrl();
            this.releaseStatus = artifactBuild.getReleaseStatus();
            this.moduleID = module;
        }
    }

    public ProduceBy() {
    }

    public String getModuleID() {
        return moduleID;
    }

    public void setModuleID(String module) {
        this.moduleID = module;
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

    public long getStarted() {
        return started;
    }

    public void setStarted(long started) {
        this.started = started;
    }

    public String getStartedString() {
        return startedString;
    }

    public void setStartedString(String startedString) {
        this.startedString = startedString;
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
}
