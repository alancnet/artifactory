package org.artifactory.ui.rest.model.builds;

import org.artifactory.api.governance.BlackDuckApplicationInfo;
import org.artifactory.api.governance.GovernanceRequestInfo;
import org.artifactory.rest.common.model.BaseModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Chen Keinan
 */
public class BuildGovernanceInfo extends BaseModel {

    private List<GovernanceRequestInfo> components = new ArrayList<>();
    private List<GovernanceRequestInfo> publishedArtifacts = new ArrayList<>();
    private Set<String> scopes = new HashSet<>();
    private BlackDuckApplicationInfo applicationInfo;

    public BuildGovernanceInfo() {
    }


    public BuildGovernanceInfo(List<GovernanceRequestInfo> components, List<GovernanceRequestInfo> publishedArtifacts,
                               Set<String> scopes, BlackDuckApplicationInfo applicationInfo) {
        this.components = components;
        this.publishedArtifacts = publishedArtifacts;
        this.scopes = scopes;
        this.applicationInfo = applicationInfo;
    }

    public List<GovernanceRequestInfo> getComponents() {
        return components;
    }

    public void setComponents(List<GovernanceRequestInfo> components) {
        this.components = components;
    }

    public List<GovernanceRequestInfo> getPublishedArtifacts() {
        return publishedArtifacts;
    }

    public void setPublishedArtifacts(List<GovernanceRequestInfo> publishedArtifacts) {
        this.publishedArtifacts = publishedArtifacts;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public BlackDuckApplicationInfo getApplicationInfo() {
        return applicationInfo;
    }

    public void setApplicationInfo(BlackDuckApplicationInfo applicationInfo) {
        this.applicationInfo = applicationInfo;
    }

}
