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
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.common.ha;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author yoavl
 */
public class HaNodeProperties {

    public static final String PROP_NODE_ID = "node.id";
    public static final String PROP_CLUSTER_HOME = "cluster.home";
    public static final String PROP_CONTEXT_URL = "context.url";
    public static final String PROP_PRIMARY = "primary";
    public static final String PROP_MEMBERSHIP_PORT = "membership.port";

    private final Properties properties = new Properties();

    public void load(@Nonnull File propsFile) {
        if (propsFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propsFile)) {
                properties.load(fis);
                verifyPropertiesInformation();
            } catch (Exception e) {
                throw new RuntimeException("Could not read ha-node properties from " +
                        "'" + propsFile.getAbsolutePath() + "': " + e.getMessage(), e);
            }
        } else {
            throw new RuntimeException("Could not find ha-node properties in '" + propsFile.getAbsolutePath() + "'.");
        }
    }

    public String getServerId() {
        return properties.getProperty(PROP_NODE_ID);
    }

    public File getClusterHome() {
        return new File(properties.getProperty(PROP_CLUSTER_HOME));
    }

    public String getContextUrl() {
        return properties.getProperty(PROP_CONTEXT_URL);
    }

    public boolean isPrimary() {
        return Boolean.valueOf(properties.getProperty(PROP_PRIMARY));
    }

    public Properties getProperties() {
        return properties;
    }

    @Nullable
    public Integer getMembershipPort() {
        String membershipPort = properties.getProperty(PROP_MEMBERSHIP_PORT);
        return StringUtils.isNotBlank(membershipPort) ? Integer.valueOf(membershipPort) : null;
    }

    public ImmutableMap<String, String> getPropertiesMap() {
        return Maps.fromProperties(properties);
    }

    private void verifyPropertiesInformation() {
        if (StringUtils.isBlank(getServerId())) {
            throw new RuntimeException(invalidProperty(PROP_NODE_ID) + ": can not be blank");
        }
        if (StringUtils.isBlank(properties.getProperty(PROP_CLUSTER_HOME))) {
            throw new RuntimeException(invalidProperty(PROP_CLUSTER_HOME) + ": can not be blank");
        }
        if (!getClusterHome().exists()) {
            throw new RuntimeException(invalidProperty(PROP_CLUSTER_HOME) + ": path " +
                    getClusterHome().getAbsolutePath() + " does not exist");
        }
        if (StringUtils.isBlank(getContextUrl())) {
            throw new RuntimeException(invalidProperty(PROP_CONTEXT_URL) + ": can not be blank");
        }
        try {
            getMembershipPort();
        } catch (NumberFormatException e) {
            throw new RuntimeException(invalidProperty(PROP_MEMBERSHIP_PORT) + ": not a valid number");
        }
    }

    private String invalidProperty(String propertyName) {
        return "Invalid property " + propertyName + " in ha-node properties";
    }
}
