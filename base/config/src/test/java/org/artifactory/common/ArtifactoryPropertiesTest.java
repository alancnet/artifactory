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

package org.artifactory.common;

import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;

import static java.lang.Integer.parseInt;
import static org.testng.Assert.assertEquals;

/**
 * @author freds
 * @date Oct 12, 2008
 */
public class ArtifactoryPropertiesTest extends ArtifactoryHomeBoundTest {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryPropertiesTest.class);

    @AfterMethod
    public void clearArtifactoryProperties() {
        // clear any property that might have been set in a test
        for (ConstantValues constantsValue : ConstantValues.values()) {
            System.clearProperty(constantsValue.getPropertyName());
        }
    }

    @Test
    public void printArtifactorySystemFile() {
        ConstantValues[] constantsValues = ConstantValues.values();
        StringBuilder builder = new StringBuilder("Default Properties:\n");
        for (ConstantValues value : constantsValues) {
            builder.append("#").append(value.getPropertyName()).append("=").append(value.getDefValue()).append("\n");
        }
        log.info(builder.toString());
    }

    @Test
    public void testLoadProps() throws URISyntaxException {
        File file = ResourceUtils.getResourceAsFile("/config/system/artifactory.system.1.properties");
        ArtifactoryHome.get().getArtifactoryProperties().loadArtifactorySystemProperties(file, null);
        assertEquals(ConstantValues.logsViewRefreshRateSecs.getInt(), 1000);
        assertEquals(ConstantValues.locksTimeoutSecs.getInt(),
                parseInt(ConstantValues.locksTimeoutSecs.getDefValue()));
        assertEquals(ConstantValues.securityAuthenticationCacheIdleTimeSecs.getInt(), 50);
        assertEquals(ConstantValues.searchMaxResults.getInt(),
                parseInt(ConstantValues.searchMaxResults.getDefValue()));
    }

    @Test
    public void testSystemProps() throws URISyntaxException {
        File file = ResourceUtils.getResourceAsFile("/config/system/artifactory.system.1.properties");
        System.setProperty(ConstantValues.securityAuthenticationCacheIdleTimeSecs.getPropertyName(), "800");

        ArtifactoryHome.get().getArtifactoryProperties().loadArtifactorySystemProperties(file, null);
        assertEquals(ConstantValues.logsViewRefreshRateSecs.getInt(), 1000);
        assertEquals(ConstantValues.locksTimeoutSecs.getInt(), 120);
        assertEquals(ConstantValues.securityAuthenticationCacheIdleTimeSecs.getInt(), 800);
        assertEquals(ConstantValues.searchMaxResults.getInt(),
                parseInt(ConstantValues.searchMaxResults.getDefValue()));
    }

    @Test
    public void defaultArtifactoryVersion() throws URISyntaxException {
        ArtifactoryHome.get().getArtifactoryProperties().loadArtifactorySystemProperties(null, null);
        Assert.assertNull(ConstantValues.artifactoryVersion.getString(), "Expected null but was " +
                ConstantValues.artifactoryVersion.getString());
        Assert.assertNull(ConstantValues.artifactoryRevision.getString(), "Expected null but was " +
                ConstantValues.artifactoryRevision.getString());
    }

    @Test
    public void artifactoryVersion() throws URISyntaxException {
        File file = ResourceUtils.getResourceAsFile("/config/system/artifactory.properties");
        ArtifactoryHome.get().getArtifactoryProperties().loadArtifactorySystemProperties(null, file);
        assertEquals(ConstantValues.artifactoryVersion.getString(), "10.3");
        assertEquals(ConstantValues.artifactoryRevision.getInt(), 12345);
    }

    @Test
    public void systemPropsOverrideArtifactoryProperties() throws URISyntaxException {
        System.setProperty(ConstantValues.artifactoryVersion.getPropertyName(), "3.0");
        System.setProperty(ConstantValues.artifactoryRevision.getPropertyName(), "5555");

        File file = ResourceUtils.getResourceAsFile("/config/system/artifactory.properties");
        ArtifactoryHome.get().getArtifactoryProperties().loadArtifactorySystemProperties(null, file);
        assertEquals(ConstantValues.artifactoryVersion.getString(), "3.0");
        assertEquals(ConstantValues.artifactoryRevision.getInt(), 5555);
    }
}
