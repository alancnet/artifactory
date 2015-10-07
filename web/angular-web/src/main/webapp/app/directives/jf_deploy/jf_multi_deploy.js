/**
 * Created by idannaim on 8/6/15.
 */
import EVENTS from '../../constants/artifacts_events.constants';
import API from '../../constants/api.constants';
import TOOLTIP from '../../constants/artifact_tooltip.constant';

class jfMultiDeployController {


    constructor($scope, ArtifactoryUploaderFactory, ArtifactDeployDao, ArtifactoryState,
            ArtifactoryNotifications) {

        this.comm.setController(this);
        this.$scope = $scope;
        this.artifactDeployDao = ArtifactDeployDao;
        this.artifactoryNotifications = ArtifactoryNotifications;
        this.artifactoryState = ArtifactoryState;
        this.artifactoryUploaderFactory = ArtifactoryUploaderFactory;
        this.errorQueue = [];
        this.multiSuccessMessage = '';
        this.multiSuccessMessageCount = 0;
        this.TOOLTIP = TOOLTIP.artifacts.deploy;
        this.originalDeployPath = '';
        this._initDeploy();
    }

    /**
     * create uploader instance,
     * set methods callback
     * set path and file type
     * @private
     */
    _initDeploy() {
        let UPLOAD_REST_URL = `${API.API_URL}/artifact/upload`;

        this.deployMultiUploader = this.artifactoryUploaderFactory.getUploaderInstance(this)
                .setUrl(UPLOAD_REST_URL)
                .setOnSuccessItem(this.onSuccessItem)
                .setOnAfterAddingAll(this.onAfterAddingAll)
                .setOnAfterAddingFile(this.onAfterAddingFile)
                .setOnErrorItem(this.onUploadError)
                .setOnCompleteAll(this.onCompleteAll)
                .setOnProgressAll(this.onProgressAll);

        this._setPathAndFileType(this.node.data.path);
    }

    /**
     * check if path includes file/archive if yes cut it from the path and set .
     * check if the current repo is local else clean path.
     * Reset garbage deployFile if exists and fields.
     * @param targetPath
     * @private
     */
    _setPathAndFileType(targetPath) {
        if (this.node.data.isInsideArchive()) {
            targetPath = "";
        }
        else {
            if (this.node.data.type == "file" || this.node.data.type == 'archive') {
                if (targetPath.indexOf('/') > -1) {
                    targetPath = targetPath.substr(0, targetPath.lastIndexOf('/'))
                }
                else if (targetPath.indexOf('\\') > -1) {
                    targetPath = targetPath.substr(0, targetPath.lastIndexOf('\\'))
                }
                else {
                    targetPath = "";
                }
            }
        }
        if (this.firstInit) {
            if (localRepo) {
                this.deployFile = {
                    repoDeploy: localRepo,
                    targetPath: targetPath
                }
            } else {
                this.deployFile = {
                    repoDeploy: this.node.data.type == 'local' ? this.node.this.reposList[0] : '',
                    targetPath: targetPath
                }
            }
        } else {
            //Reset garbage deployFile if exists
            if (this.deployFile && this.deployFile.unitInfo && this.deployFile.unitInfo.mavenArtifact) {
                this.deployFile.unitInfo.mavenArtifact = false;
            }
            if (this.deployFile && this.deployFile.unitInfo && this.deployFile.unitInfo.debianArtifact) {
                this.deployFile.unitInfo.debianArtifact = false;
            }
            this.deployFile.unitInfo = {};
            this.deployFile.fileName = '';
            this.deployMultiUploader.clearQueue();
            this.deployFile.targetPath = targetPath;
        }
        this.uploadCompleted = false;
        this.firstInit = false;
    }

    onSuccessItem(fileDetails, response) {
        this.deployFile.unitInfo = response.unitInfo;
        this.deployFile.unitConfigFileContent = response.unitConfigFileContent;
        //MavenArtifact causes 'deploy as' checkbox to be lit -> change deployment path according to GAVC
        if (this.deployFile.unitInfo && this.deployFile.unitInfo.mavenArtifact) {
            this.originalDeployPath = this.deployFile.targetPath;
        }
        if (response.repoKey && response.artifactPath) {
            let msg = this.comm.createNotification(response);
            this.multiSuccessMessage += msg.body + '<br>';
            this.multiSuccessMessageCount++;
        }
    }

    /**
     * check if queue have files to upload
     */
    multiUploadItemRemoved() {
        if (!this.deployMultiUploader.getQueue() || !this.deployMultiUploader.getQueue().length) {
            this.uploadCompleted = false;
        }
    }

    /**
     * when upload item error push to queue for notifications
     * @param item
     * @param response
     */
    onUploadError(item, response) {

        this.errorQueue.push({item: item, response: response});

    }

    /**
     * upload complete check if 'error queue' if empty if not show all failed files
     * else show success notification
     */
    onCompleteAll() {
        this.uploadCompleted = true;
        this.progress = false;
        let body = '<ul>';
        this.artifactoryNotifications.clear();
        if (this.errorQueue.length) {
            this.errorQueue.forEach((error)=> {
                body += '<li>"' + error.item.file.name + '" ' + error.response.error + '</li>'
            })
            body += '</ul>';
            this.artifactoryNotifications.createMessageWithHtml({type: 'error', body: body, timeout: 10000});
            this.deployMultiUploader.clearQueue();
            this.errorQueue = [];
        }
        else if (this.onSuccess && typeof this.onSuccess === 'function') {
            this.artifactoryNotifications.createMessageWithHtml({type: 'success', body: `Successfully deployed ${this.multiSuccessMessageCount} files`});
            this.onSuccess();
        }
    }

    /**
     * onAfterAddingAll check for only 20 files  upload
     * @param fileItems
     */
    onAfterAddingAll(fileItems) {
        if (fileItems.length > 20) {
            this.artifactoryNotifications.create({error: "You can only deploy up to 20 files at a time"});
            this.deployMultiUploader.clearQueue();
            return;
        }
        //Enable the "deploy" button after all files were added.
        this.uploadCompleted = true;
        let uploadAll = true;

        fileItems.forEach((item)=> {
            if (!item.okToUploadFile) {

                uploadAll = false;
                return;
            }
        });
    }

    /**
     * check if missing fields to disable deploy button
     * @returns {boolean|*}
     */
    isReady() {
        let ok = true;
        if (this.deployFile.unitInfo && this.deployFile.unitInfo.debianArtifact) {
            ok = this.deployFile.unitInfo.distribution && this.deployFile.unitInfo.component && this.deployFile.unitInfo.architecture;
        }
        return ok && this.uploadCompleted;
    }

    onAfterAddingFile(fileItem) {
        if (fileItem.file.size < 0) {
            fileItem.okToUploadFile = false;
            this.deployMultiUploader.removeFileFromQueue(fileItem);
        }
        else {
            // Save original for display
            fileItem.file.originalName = fileItem.file.name;
            // Encode filename to support UTF-8 strings (server does decode)
            fileItem.file.name = encodeURI(fileItem.file.name);
            fileItem.okToUploadFile = true;
        }
    }

    onProgressAll() {
        if (!this.progress) {
            this.progress = true;
            this.artifactoryNotifications.createMessageWithHtml({
                type: 'success',
                body: 'Deploy in progress...',
                timeout: 60 * 60000
            });
        }
    }

    /**
     * if exist char '&' need to be replace to  '%26' before upload
     * @param name
     * @returns {*}
     * @private
     */
    _fixUrlPath(name) {
        name = name.replace(/&/g, '%26');
        var find = '&';
        var re = new RegExp(find, 'g');
        return name.replace(re, '%26');
    }

    /**
     * set url to deploy for each file and deploy when ready
     */
    deployArtifacts() {

        let DEPLOY_REST_URL = `${API.API_URL}/artifact/deploy/multi`;

        if ((!this.deployFile.targetPath.endsWith("/"))) {
            this.deployFile.targetPath += "/";
        }

        this.deployMultiUploader.getQueue().forEach((item)=> {
            item.url = DEPLOY_REST_URL + '?repoKey=' + this.deployFile.repoDeploy.repoKey + '&path=' +
            (this.deployFile.targetPath || '') + this._fixUrlPath(item.file.name);
        });
        this.deployMultiUploader.getUploader().uploadAll();
    }


}
export function jfMultiDeploy() {
    return {
        restrict: 'EA',
        scope: {
            node: '=',
            deploy: '&',
            comm: '=',
            deployFile: '=',
            onSuccess: '&'
        },
        controller: jfMultiDeployController,
        controllerAs: 'jfMultiDeploy',
        bindToController: true,
        templateUrl: 'directives/jf_deploy/jf_multi_deploy.html'
    }
}