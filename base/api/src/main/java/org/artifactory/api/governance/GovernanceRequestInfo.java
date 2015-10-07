package org.artifactory.api.governance;

import com.google.common.collect.Sets;
import org.artifactory.repo.RepoPath;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Chen Keinan
 */
@JsonIgnoreProperties("repoPath")
public class GovernanceRequestInfo {

    ExtComponentCoordinates componentCoordinates;
    private String componentName;
    private String componentVersion;
    private Set<String> scopes = Sets.newHashSet();
    private boolean published;
    private RepoPath repoPath;
    private String license;
    private String componentId; //reflects catalog component id
    private List<ExternalVulnerability> vulnerabilities;
    private String status;
    private String repoKey;
    private String path;
    private List<String> actions;
    private String requestLink;


    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentVersion(String componentVersion) {
        this.componentVersion = componentVersion;
    }

    public String getComponentVersion() {
        return componentVersion;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getLicense() {
        return license;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        if (status != null && (status.equals("Missing") || status.equals("Stale"))) {
            if (actions == null) {
                actions = new ArrayList<>();
            }
            actions.add("UpdateRequest");
        }
    }

    public String getCoordinates() {
        return getComponentCoordinates().getCoordinates();
    }

    public String getMailCompatibleCoordinates() {
        return getComponentCoordinates().getMailCompatibleCoordinates();
    }

    public String getExternalId() {
        return getComponentCoordinates().getExternalId();
    }

    public String getNameSpace() {
        return componentCoordinates.getNamespace();
    }

    public void setComponentCoordinates(ExtComponentCoordinates coordinates) {
        this.componentCoordinates = coordinates;
    }

    public ExtComponentCoordinates getComponentCoordinates() {
        return componentCoordinates;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    public void setRepoPath(RepoPath repoPath) {
        this.repoPath = repoPath;
        if (repoPath != null) {
            repoKey = repoPath.getRepoKey();
            path = repoPath.getPath();
            if (actions == null) {
                actions = new ArrayList<>();
            }
            actions.add("ShowInTree");
        } else {
            repoKey = "";
            path = "No path found (externally resolved or deleted/overwritten)";
        }
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    @Nullable
    public List<ExternalVulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(@Nullable List<ExternalVulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public String getRequestLink() {
        return requestLink;
    }

    public void setRequestLink(String requestLink) {
        this.requestLink = requestLink;
        if (requestLink != null) {
            if (actions == null) {
                actions = new ArrayList<>();
            }
            actions.add("ShowRequest");
        }
    }

    @Override
    public String toString() {
        return "BlackDuckRequestInfo{" +
                "componentName='" + componentName + '\'' +
                ", componentVersion='" + componentVersion + '\'' +
                ", Code Center Namespace='" + componentCoordinates.getNamespace() + '\'' +
                ", coordinates='" + componentCoordinates.getCoordinates() + '\'' +
                ", componentId='" + componentId + '\'' +
                '}';
    }
}
