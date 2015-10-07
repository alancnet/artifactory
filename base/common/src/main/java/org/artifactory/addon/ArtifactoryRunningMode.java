package org.artifactory.addon;

/**
 * author: gidis
 * Represent the actualArtifactory mode which can be one of the following OSS,PRO,HA,AOL
 */
public enum ArtifactoryRunningMode {
    OSS, PRO, HA;

    public static ArtifactoryRunningMode fromString(String val) {
        // Artifactory always have its running mode so throw exception if fail to find proper enum.
        return valueOf(val.toUpperCase());
    }

    public boolean isHa() {
        return HA == this;
    }

    public static boolean sameMode(ArtifactoryRunningMode... runningModes) {
        ArtifactoryRunningMode last = runningModes[0];
        for (ArtifactoryRunningMode runningMode : runningModes) {
            if (runningMode != last) {
                return false;
            }
        }
        return true;
    }
}
