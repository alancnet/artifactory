package org.artifactory.addon.license;

import org.artifactory.common.ArtifactoryHome;

/**
 * Author: gidis
 * Helps to manage and transfer the servers members verification result
 */
public enum VerificationResult {
    valid, noLicense, invalidKey, error, duplicateServerIds, duplicateLicense, converting, notSameVersion,
    runningModeConflict, haConfiguredNotHaLicense;

    public boolean isValid() {
        return this == valid;
    }

    public String showMassage() {
        switch (this) {
            case valid: {
                return "Valid.";
            }
            case invalidKey: {
                return "Invalid Artifactory license.";
            }
            case duplicateServerIds: {
                return "Stopping Artifactory since duplicate node ids have been found in registry. " +
                        "If you restarted this server, make sure to wait at least 30 seconds before re-activating it.";
            }
            case duplicateLicense: {
                return "Changing Artifactory mode to offline since duplicate license has been found in registry.";
            }
            case runningModeConflict: {
                if (ArtifactoryHome.get().isHaConfigured()) {
                    return "Changing Artifactory mode to offline since the local server is running as HA " +
                            "but found none HA server in registry.";
                } else {
                    return "Changing Artifactory mode to offline since the local server is running as PRO/OSS " +
                            "but found other servers in registry.";
                }
            }
            case converting: {
                return "Stopping Artifactory start up ,another server running converting process.";
            }
            case notSameVersion: {
                return "Stopping Artifactory start up ,another server with different version has been found.";
            }
            case noLicense: {
                return "Changing Artifactory mode to offline since no license is installed and other servers have " +
                        "been found in registry. Try to install HA license and then restart the server.";
            }
            case haConfiguredNotHaLicense: {
                return "Changing Artifactory mode to offline since the server is configured as HA but the license " +
                        "is either not exist or not HA License.";
            }
            default: {
                return "Error occurred during license verification/installation.";
            }
        }
    }

    public static VerificationResult and(VerificationResult... values) {
        for (VerificationResult value : values) {
            if (valid != value) {
                return value;
            }
        }
        return valid;
    }

    public static VerificationResult or(VerificationResult... values) {
        VerificationResult result = valid;
        for (VerificationResult value : values) {
            if (valid == value) {
                return valid;
            } else {
                result = value;
            }
        }
        return result;
    }
}
