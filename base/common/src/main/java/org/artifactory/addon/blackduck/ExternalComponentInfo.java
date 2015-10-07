package org.artifactory.addon.blackduck;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class ExternalComponentInfo {

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
}
