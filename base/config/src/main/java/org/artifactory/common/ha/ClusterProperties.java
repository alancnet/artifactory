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

import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author yoavl
 */
public class ClusterProperties {

    public static final String SECURITY_TOKEN_PROPERTY_KEY = "security.token";

    private Properties properties = new Properties();

    public void load(@Nonnull File propsFile) {
        if (propsFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propsFile)) {
                properties.load(fis);
                verifyPropertiesInformation();
            } catch (Exception e) {
                throw new RuntimeException("Could not read cluster properties from " +
                        "'" + propsFile.getAbsolutePath() + "': " + e.getMessage(), e);
            }
        } else {
            throw new RuntimeException("Could not find cluster properties in '" + propsFile.getAbsolutePath() + "'.");
        }
    }

    public String getSecurityToken() {
        return properties.getProperty(SECURITY_TOKEN_PROPERTY_KEY);
    }

    private void verifyPropertiesInformation() {
        if (StringUtils.isBlank(getSecurityToken())) {
            throw new RuntimeException("Bad security token found in cluster properties");
        }
    }
}
