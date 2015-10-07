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
package org.artifactory.security.mission.control;

import org.apache.commons.lang.StringUtils;
import org.artifactory.storage.LinkedProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Gidi Shabat
 */
public class MissionControlProperties {
    private static final Logger log = LoggerFactory.getLogger(MissionControlProperties.class);
    private LinkedProperties props = null;

    public MissionControlProperties(File missionControlPropsFile) {
        props = new LinkedProperties();
        if (missionControlPropsFile != null && missionControlPropsFile.exists()) {
            try {
                try (FileInputStream pis = new FileInputStream(missionControlPropsFile)) {
                    props.load(pis);
                }
                trimValues();
                assertMandatoryProperties();
            } catch (Exception e) {
                log.debug("Mission Control properties exist but contain invalid data", e);
            }
        }

    }

    private void trimValues() {
        Iterator<Map.Entry<String, String>> iter = props.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            String value = entry.getValue();
            if (!StringUtils.trimToEmpty(value).equals(value)) {
                entry.setValue(StringUtils.trim(value));
            }
        }
    }

    public String getUrl() {
        return props.getProperty("url", null);
    }
    public String getCreatedBy() {
        return props.getProperty("created.by", null);
    }
    public String getCreatedDate() {
        return props.getProperty("created.date", null);
    }

    public void setUrl(String url) {
        props.setProperty("url", url);
    }

    public void setCreatedBy(String userName) {
        props.setProperty("created.by", userName);
    }

    public void setCreatedDate(long date) {
        props.setProperty("created.date", ""+date);
    }

    public String getToken() {
        return props.getProperty("token", null);
    }

    public void setToken(String token) {
        props.setProperty("token", token);
    }

    private void assertMandatoryProperties() {
        if (StringUtils.isBlank(getUrl())) {
            throw new RuntimeException("Missing URL field in Mission Control properties file");
        }
        if (StringUtils.isBlank(getToken())) {
            throw new RuntimeException("Missing Token field in Mission Control properties file");
        }

    }

    public void updateMissionControlPropertiesFile(File missionControlPropertiesFile) throws IOException {
        if (props != null &&  props.iterator().hasNext()) {
            OutputStream outputStream = new FileOutputStream(missionControlPropertiesFile);
            props.store(outputStream, "");
        }

    }
}