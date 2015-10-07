/**
 * Created by idannaim on 8/4/15.
 */
import{ArtifactoryUploaderFactory}   from'./artifactory_uploader'

export default angular.module('artifactory_uploader', ['angularFileUpload'])
        .service('ArtifactoryUploaderFactory', ArtifactoryUploaderFactory)