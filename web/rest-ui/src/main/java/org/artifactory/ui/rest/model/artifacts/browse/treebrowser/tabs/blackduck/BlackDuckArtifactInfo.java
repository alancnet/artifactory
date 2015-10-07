package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.blackduck;

import java.util.List;

import org.artifactory.addon.blackduck.BlackDuckVulnerabilities;
import org.artifactory.addon.blackduck.BlackduckInfo;
import org.artifactory.addon.blackduck.LicensePair;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

/**
 * @author Chen Keinan
 */
public class BlackDuckArtifactInfo extends BaseArtifactInfo implements RestModel {

    public BlackDuckArtifactInfo(String name) {
        super(name);
    }

    public BlackDuckArtifactInfo() {
    }

    private String componentId;
    private String origComponentId;
    private BlackduckInfo info;
    private List<LicensePair> license;
    private List<BlackDuckVulnerabilities> vulnerabilities;

    public BlackduckInfo getInfo() {
        return info;
    }

    public void setInfo(BlackduckInfo info) {
        this.info = info;
    }

    public List<LicensePair> getLicense() {
        return license;
    }

    public void setLicense(List<LicensePair> license) {
        this.license = license;
    }

    public List<BlackDuckVulnerabilities> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(List<BlackDuckVulnerabilities> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getOrigComponentId() {
        return origComponentId;
    }

    public void setOrigComponentId(String origComponentId) {
        this.origComponentId = origComponentId;
    }
}
