package org.artifactory.storage.db.servers.model;

import org.apache.commons.lang.StringUtils;

/**
 * @author mamo, fsi
 */
public enum ArtifactoryServerRole {

    STANDALONE, PRIMARY, MEMBER, COPY;

    public static ArtifactoryServerRole fromString(String val) {
        if (StringUtils.isBlank(val)) {
            return STANDALONE;
        }
        try {
            return valueOf(val.toUpperCase());
        } catch (IllegalArgumentException e) {
            return STANDALONE;
        }
    }

    public String getPrettyName() {
        return StringUtils.capitalize(name().toLowerCase());
    }
}
