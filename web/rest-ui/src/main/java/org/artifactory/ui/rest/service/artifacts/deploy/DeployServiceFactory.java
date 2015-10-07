package org.artifactory.ui.rest.service.artifacts.deploy;

import org.springframework.beans.factory.annotation.Lookup;

/**
 * @author Chen Keinan
 */
public abstract class DeployServiceFactory {

    // upload  artifact
    @Lookup
    public abstract ArtifactUploadService artifactUpload();

    // upload  artifact bundle
    @Lookup
    public abstract ArtifactDeployBundleService artifactDeployBundle();

    // deploy  artifact
    @Lookup
    public abstract ArtifactDeployService deployArtifact();

    // deploy  artifact
    @Lookup
    public abstract CancelArtifactUpload cancelArtifactUpload();

    @Lookup
    public abstract ArtifactMultiDeployService artifactMultiDeploy();

}
