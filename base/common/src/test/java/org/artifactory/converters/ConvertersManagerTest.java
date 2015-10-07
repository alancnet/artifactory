package org.artifactory.converters;

import org.artifactory.version.ArtifactoryVersion;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.artifactory.converters.helpers.ConvertersManagerTestHelper.*;
import static org.artifactory.version.ArtifactoryVersion.v301;
import static org.artifactory.version.ArtifactoryVersion.v310;

/**
 * @author Gidi Shabat
 */
@Test

public class ConvertersManagerTest {

    /**
     * Convert artifactory which home is version 3.0.1  DBProperties is 3.0.1 and cluster version is 3.0.1
     */
    public void convertAll301() throws IOException {
        createEnvironment(v301, v301, v301);
        // Make sure that the artifactory local home properties.file has been updated
        Assert.assertTrue(isArtifactoryLocalHomePropertiesHasBeenUpdatedToCurrent());
        Assert.assertTrue(isArtifactoryLocalHomePropertiesHasBeenUpdated());
        // Make sure that the artifactory local home properties.file has been updated
        Assert.assertTrue(isArtifactoryClusterHomePropertiesHasBeenUpdatedToCurrent());
        Assert.assertTrue(isArtifactoryClusterHomePropertiesHasBeenUpdated());
        // Make sure that the artifactory db properties has been updated
        Assert.assertTrue(isArtifactoryDBPropertiesHasBeenUpdated());
    }


    /**
     * Convert artifactory which home is version 3.0.4  DBProperties is 3.0.4 and cluster version is 3.0.4
     */
    public void convertAll304() throws IOException {
        createEnvironment(v301, v301, v301);
        // Make sure that the artifactory local home properties.file has been updated
        Assert.assertTrue(isArtifactoryLocalHomePropertiesHasBeenUpdatedToCurrent());
        Assert.assertTrue(isArtifactoryLocalHomePropertiesHasBeenUpdated());
        // Make sure that the artifactory local home properties.file has been updated
        Assert.assertTrue(isArtifactoryClusterHomePropertiesHasBeenUpdatedToCurrent());
        Assert.assertTrue(isArtifactoryClusterHomePropertiesHasBeenUpdated());
        // Make sure that the artifactory db properties has been updated
        Assert.assertTrue(isArtifactoryDBPropertiesHasBeenUpdated());
    }

    /**
     * Convert artifactory which home is version current  DBProperties is current and cluster version is current
     */
    public void convertAllCurrent() throws IOException {
        ArtifactoryVersion current = ArtifactoryVersion.getCurrent();
        createEnvironment(current, current, current);
        // Make sure that the artifactory local home properties.file has been updated
        Assert.assertFalse(isArtifactoryLocalHomePropertiesHasBeenUpdated());
        // Make sure that the artifactory local home properties.file has been updated
        Assert.assertFalse(isArtifactoryClusterHomePropertiesHasBeenUpdated());
        // Make sure that the artifactory db properties has been updated
        Assert.assertFalse(isArtifactoryDBPropertiesHasBeenUpdated());
    }

    /**
     * Convert artifactory which home is version null  DBProperties is null and cluster version is null
     */
    public void convertNoVersions() throws IOException {
        createEnvironment(null, null, null);
        // Make sure that the artifactory local home properties.file has been updated
        Assert.assertFalse(isArtifactoryLocalHomePropertiesHasBeenUpdated());
        // Make sure that the artifactory local home properties.file has been updated
        Assert.assertFalse(isArtifactoryClusterHomePropertiesHasBeenUpdated());
        // Make sure that the artifactory db properties has been updated
        Assert.assertTrue(isArtifactoryDBPropertiesHasBeenUpdated());
    }

    /**
     * Convert artifactory which home is current  DBProperties is 3.0.1 and cluster version is current
     * <p/>
     * In case that the db_properties doesn't exist then take the version from home.
     * This is dangerous: in case that the home version is current no db conversion will run
     */
    public void convertCurrentHomeNoDbCurrentCluster() throws IOException {
        ArtifactoryVersion current = ArtifactoryVersion.getCurrent();
        createEnvironment(current, v301, current);
        // Make sure that the artifactory local home properties.file has been updated
        Assert.assertFalse(isArtifactoryLocalHomePropertiesHasBeenUpdated());
        // Make sure that the artifactory local home properties.file has been updated
        Assert.assertFalse(isArtifactoryClusterHomePropertiesHasBeenUpdated());
        // Make sure that the artifactory db properties has been updated
        Assert.assertTrue(isArtifactoryDBPropertiesHasBeenUpdated());
    }

    /**
     * * Convert artifactory which home is version current  DBProperties is 3.1.0 and cluster version is current
     */
    public void convertCurrentHomeOldDbCurrentCluster() throws IOException {
        ArtifactoryVersion current = ArtifactoryVersion.getCurrent();
        createEnvironment(current, v310, current);
        // Make sure that the artifactory local home properties.file has been updated
        Assert.assertFalse(isArtifactoryLocalHomePropertiesHasBeenUpdated());
        // Make sure that the artifactory local cluster properties.file has been updated
        Assert.assertFalse(isArtifactoryClusterHomePropertiesHasBeenUpdated());
        // Make sure that the artifactory db properties has been updated
        Assert.assertTrue(isArtifactoryDBPropertiesHasBeenUpdated());
    }


    /**
     * Convert artifactory which home is version 3.0.1  DBProperties is current and cluster version is current
     */
    public void convertOldHomeCurrentDbCurrentCluster() throws IOException {
        ArtifactoryVersion current = ArtifactoryVersion.getCurrent();
        createEnvironment(v301, current, current);
        // Make sure that the artifactory local home properties.file has been updated
        Assert.assertTrue(isArtifactoryLocalHomePropertiesHasBeenUpdatedToCurrent());
        Assert.assertTrue(isArtifactoryLocalHomePropertiesHasBeenUpdated());
        // Make sure that the artifactory cluster home properties.file has been updated
        Assert.assertFalse(isArtifactoryClusterHomePropertiesHasBeenUpdated());
        // Make sure that the artifactory db properties has been updated
        Assert.assertFalse(isArtifactoryDBPropertiesHasBeenUpdated());
    }

    /**
     * Convert artifactory which home is version current  DBProperties iscurrent and cluster version is 3.0.1
     */
    public void convertCurrentHomeCurrentDbOldCluster() throws IOException {
        ArtifactoryVersion current = ArtifactoryVersion.getCurrent();
        createEnvironment(current, current, v301);
        // Make sure that the artifactory local home properties.file has been updated
        Assert.assertFalse(isArtifactoryLocalHomePropertiesHasBeenUpdated());
        // Make sure that the artifactory cluster home properties.file has been updated
        Assert.assertTrue(isArtifactoryClusterHomePropertiesHasBeenUpdatedToCurrent());
        Assert.assertTrue(isArtifactoryClusterHomePropertiesHasBeenUpdated());
        // Make sure that the artifactory db properties has been updated
        Assert.assertFalse(isArtifactoryDBPropertiesHasBeenUpdated());
    }
}






