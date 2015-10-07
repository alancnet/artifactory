package org.artifactory.state;

import org.apache.commons.lang.StringUtils;

/**
 * @author mamo, fsi
 */
public enum ArtifactoryServerState {
    UNKNOWN, OFFLINE, STARTING, RUNNING, STOPPING, STOPPED, CONVERTING;

    public static ArtifactoryServerState fromString(String val) {
        if (StringUtils.isBlank(val)) {
            return UNKNOWN;
        }
        try {
            return valueOf(val.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    public String getPrettyName() {
        return StringUtils.capitalize(name().toLowerCase());
    }
}
