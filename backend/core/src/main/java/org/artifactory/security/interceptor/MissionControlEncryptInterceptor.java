/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 */
package org.artifactory.security.interceptor;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.security.mission.control.MissionControlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Gidi Shabat
 */
public class MissionControlEncryptInterceptor {
    private static final Logger log = LoggerFactory.getLogger(MissionControlEncryptInterceptor.class);

    /**
     * encrypt or decrypt mission control properties file
     */
    public void encryptOrDecryptMissionControlPropertiesFile(boolean encrypt) {
        try {
            MissionControlProperties missionControlProperties = ContextHelper.get().beanForType(MissionControlProperties.class);
            String password = missionControlProperties.getToken();
            if (StringUtils.isNotBlank(password)) {
                missionControlProperties.setToken(getNewPassword(encrypt, password));
            }
            ArtifactoryHome artifactoryHome = ArtifactoryHome.get();
            missionControlProperties.updateMissionControlPropertiesFile(artifactoryHome.getMissionControlPropertiesFile());
        } catch (IOException e) {
            log.error("Error during encrypt decrypt Mission Control properties File" + e.getMessage(), e, log);
        }
    }

    private String getNewPassword(boolean encrypt, String password) {
        if (StringUtils.isNotBlank(password)) {
            if (encrypt) {
                return CryptoHelper.encryptIfNeeded(password);
            } else {
                return CryptoHelper.decryptIfNeeded(password);
            }
        }
        return null;
    }

}
