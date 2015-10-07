package org.artifactory.converters;

import org.apache.commons.io.FileUtils;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.property.FatalConversionException;
import org.artifactory.converters.helpers.MockArtifactoryContext;
import org.artifactory.converters.helpers.MockArtifactoryHome;
import org.artifactory.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.artifactory.common.ArtifactoryHome.LOGBACK_CONFIG_FILE_NAME;
import static org.artifactory.common.ArtifactoryHome.MIME_TYPES_FILE_NAME;
import static org.artifactory.converters.helpers.ConvertersManagerTestHelper.*;
import static org.artifactory.version.ArtifactoryVersion.v301;

/**
 * The test makes sure to revert the logBack and the mimetype files in case of invalid license
 *
 * @author Gidi Shabat
 */
@Test
public class ConvertersManagerWithRevertBackupTest {

    /**
     * Revert home conversions in case of invalid license
     */
    public void ensureRevertHomeConversionOnInvalidLicense() throws IOException {
        // Update Artifactory home files to be v301 compliant
        createHomeEnvironment(v301, v301);
        // Create ArtifactoryHome
        ArtifactoryHome artifactoryHome = new MockArtifactoryHome();
        // Create versionProvider
        VersionProviderImpl versionProvider = new VersionProviderImpl(artifactoryHome);
        // Create converter manager
        ConvertersManagerImpl convertersManager = new ConvertersManagerImpl(artifactoryHome, versionProvider);
        // Run home conversion
        File logBackSourceFile = new File(ArtifactoryHome.get().getEtcDir(), LOGBACK_CONFIG_FILE_NAME);
        String logBackSource301 = FileUtils.readFileToString(logBackSourceFile);
        File mimeTypeSourceFile = new File(ArtifactoryHome.get().getEtcDir(), MIME_TYPES_FILE_NAME);
        String mimeTypeSource301 = FileUtils.readFileToString(mimeTypeSourceFile);
        convertersManager.convertHomes();
        // Now bind ArtifactoryContext and artifactoryHome like in real environment
        MockArtifactoryContext artifactoryContext = new MockArtifactoryContext(v301, 1, convertersManager,
                versionProvider, false);
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
        ArtifactoryHome.bind(artifactoryHome);
        ((VersionProviderImpl) artifactoryContext.getVersionProvider()).loadDbVersion();
        // Now Ensure that all backup files exist
        File logBackBackupFile = new File(ArtifactoryHome.get().getEtcDir(), LOGBACK_CONFIG_FILE_NAME + ".back");
        File mimeTypeBackupFile = new File(ArtifactoryHome.get().getEtcDir(), MIME_TYPES_FILE_NAME + ".back");
        Assert.assertTrue(logBackBackupFile.exists());
        Assert.assertTrue(mimeTypeBackupFile.exists());
        try {
            convertersManager.beforeInits();
        } catch (FatalConversionException e) {
            // Now Ensure that all backup files were deleted
            Assert.assertFalse(logBackBackupFile.exists());
            Assert.assertFalse(mimeTypeBackupFile.exists());
            // Ensure logBack has been reverted
            File revert = new File(ArtifactoryHome.get().getEtcDir(), LOGBACK_CONFIG_FILE_NAME);
            String newLogBack = FileUtils.readFileToString(revert);
            Assert.assertEquals(newLogBack, logBackSource301);
            // Ensure mimeType has been reverted
            File mimeTypeRevert = new File(ArtifactoryHome.get().getEtcDir(), MIME_TYPES_FILE_NAME);
            String newMimeType = FileUtils.readFileToString(mimeTypeRevert);
            Assert.assertEquals(newMimeType, mimeTypeSource301);
            return;
        }
        Assert.fail();
    }

    /**
     * Complete home conversions in case of valid license
     */
    public void ensureCompleteHomeConversionOnValidLicense() throws IOException {
        // Update Artifactory home files to be v301 compliant
        createHomeEnvironment(v301, v301);
        // Create ArtifactoryHome
        ArtifactoryHome artifactoryHome = new MockArtifactoryHome();
        // Create versionProvider
        VersionProviderImpl versionProvider = new VersionProviderImpl(artifactoryHome);
        // Create converter manager
        ConvertersManagerImpl convertersManager = new ConvertersManagerImpl(artifactoryHome, versionProvider);
        // Run home conversion
        convertersManager.convertHomes();
        // Now bind ArtifactoryContext and artifactoryHome like in real environment
        MockArtifactoryContext artifactoryContext = new MockArtifactoryContext(v301, 1, convertersManager,
                versionProvider, true);
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
        ArtifactoryHome.bind(artifactoryHome);
        ((VersionProviderImpl) artifactoryContext.getVersionProvider()).loadDbVersion();
        // Make sure that the backup files exist
        File logBackBackupFile = new File(ArtifactoryHome.get().getEtcDir(), LOGBACK_CONFIG_FILE_NAME + ".back");
        File mimeTypeBackupFile = new File(ArtifactoryHome.get().getEtcDir(), MIME_TYPES_FILE_NAME + ".back");
        Assert.assertTrue(logBackBackupFile.exists());
        Assert.assertTrue(mimeTypeBackupFile.exists());
        // Call "beforeInits" event and make sure that all the changes have been reverted
        convertersManager.beforeInits();
        // Ensure that backup files were deleted
        Assert.assertFalse(logBackBackupFile.exists());
        Assert.assertFalse(mimeTypeBackupFile.exists());

        // Ensure logBack has not been reverted
        File logBackRevert = new File(ArtifactoryHome.get().getEtcDir(), LOGBACK_CONFIG_FILE_NAME);
        String newLogBack = FileUtils.readFileToString(logBackRevert);
        String pathToLogBack301 = "/converters/templates/home/3.0.1";
        String logBack301 = ResourceUtils.getResourceAsString(pathToLogBack301 + "/" + LOGBACK_CONFIG_FILE_NAME);
        Assert.assertNotEquals(newLogBack, logBack301);

        // Ensure mimeType has not been reverted
        File mimeTypeRevert = new File(ArtifactoryHome.get().getEtcDir(), MIME_TYPES_FILE_NAME);
        String newMimeType = FileUtils.readFileToString(mimeTypeRevert);
        String pathToMimeType301 = "/converters/templates/home/3.0.1";
        String mimeType301 = ResourceUtils.getResourceAsString(pathToMimeType301 + "/" + MIME_TYPES_FILE_NAME);
        Assert.assertNotEquals(newMimeType, mimeType301);
    }

}
