/**
 * Created by idannaim on 8/6/15.
 */

import EVENTS from '../../constants/artifacts_events.constants';
import API from '../../constants/api.constants';
import TOOLTIP from '../../constants/artifact_tooltip.constant';

class jfSingleDeployController {
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
        this.TOOLTIP = TOOLTIP.artifacts.deploy;
        this.originalDeployPath = '';
        this.firstInit = true;
        this.uploadCompleted = false;
        this._initDeploy();
        this._initEvent();
    }

    /**
     * create uploader instance,
     * set methods callback
     * set path and file type
     * @private
     */
    _initDeploy() {
        let UPLOAD_REST_URL = `${API.API_URL}/artifact/upload`;
        this.deploySingleUploader = this.artifactoryUploaderFactory.getUploaderInstance(this)
                .setUrl(UPLOAD_REST_URL)
                .setOnSuccessItem(this.onSuccessItem)
                .setOnAfterAddingAll(this.onAfterAddingAll)
                .setOnAfterAddingFile(this.onAfterAddingFile)
                .setOnErrorItem(this.onUploadError)
                .setOnCompleteAll(this.onCompleteAll);

        this._setPathAndFileType(this.node.data.path);

        // set data from currently open node
    }

    _initEvent() {
        this.$scope.$on('$destroy', this.onRemoveSingle.bind(this));
    }

    /**
     *  On file successfully uploaded: setting path for deploy.
     *  if maven repo set fields and path
     * @param fileDetails
     * @param response
     */
    onSuccessItem(fileDetails, response) {
        response.unitInfo.debianArtifact = response.unitInfo.artifactType==='debian';
        response.unitInfo.mavenArtifact = response.unitInfo.artifactType==='maven';

        this.deployFile.unitInfo = response.unitInfo;
        this.deployFile.unitConfigFileContent = response.unitConfigFileContent;
        //MavenArtifact causes 'deploy as' checkbox to be lit -> change deployment path according to GAVC
        if (this.deployFile.unitInfo && this.deployFile.unitInfo.mavenArtifact) {
            this.originalDeployPath = this.deployFile.targetPath;
            this.updateMavenTargetPath()
        }
        if (this.comm) {
            this.needToCancel = true;
        }
    }

    /**
     * check if path includes file/archive if yes cut it from the path and set .
     * check if the current repo is local else clean path.
     * Reset garbage deployFile and fields if exists.
     * @param targetPath
     * @private
     */
    _setPathAndFileType(targetPath) {
        if (this.node.data.isInsideArchive()) {
            targetPath = "";
        }
        else {
            if (this.node.data.isFile() || this.node.data.isArchive()) {
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
            if (this.comm && this.comm.localRepo) {
                this.deployFile = {
                    repoDeploy: this.comm.localRepo,
                    targetPath: targetPath
                }
            } else {
                this.deployFile = {
                    repoDeploy: this.node.data.type == 'local' ? this.comm.reposList[0] : '',
                    targetPath: targetPath
                }
            }
        } else {
            if (this.deployFile && this.deployFile.unitInfo && this.deployFile.unitInfo.mavenArtifact) {
                this.deployFile.unitInfo.mavenArtifact = false;
            }
            if (this.deployFile && this.deployFile.unitInfo && this.deployFile.unitInfo.debianArtifact) {
                this.deployFile.unitInfo.debianArtifact = false;
            }
            this.deployFile.unitInfo = {};
            this.deployFile.fileName = '';
            this.deploySingleUploader.clearQueue();
            this.deployFile.targetPath = targetPath;
        }
        this.uploadCompleted = false;
        this.firstInit = false;
    }

    /**
     * if maven file upload
     * update path by gavc (onChange)
     */
    updateMavenTargetPath() {
        let newPath = '';
        if (this.deployFile.unitInfo.groupId) {
            newPath += this.deployFile.unitInfo.groupId.replace(/\./g, '/');
        }
        newPath += '/' + (this.deployFile.unitInfo.artifactId || '');
        newPath += '/' + (this.deployFile.unitInfo.version || '');
        newPath += '/' + (this.deployFile.unitInfo.artifactId || '');
        newPath += '-' + (this.deployFile.unitInfo.version || '');
        if (this.deployFile.unitInfo.classifier) {
            newPath += '-' + this.deployFile.unitInfo.classifier;
        }
        newPath += '.' + (this.deployFile.unitInfo.type || '');

        this.deployFile.targetPath = newPath;
        this._bindToPomXml();
    }

    /**
     * bind and update maven  xml (depend on updateMavenTargetPath)
     * @private
     */
    _bindToPomXml() {
        if (typeof window.DOMParser != 'undefined' && typeof window.XMLSerializer != 'undefined'
                && this.deployFile.unitConfigFileContent) {
            //Parse the code mirror model into xml object and modify based on input fields
            let parser = new DOMParser();
            let pomXml = parser.parseFromString(this.deployFile.unitConfigFileContent, "text/xml");
            let groupId = pomXml.getElementsByTagName('groupId');
            if (groupId.length) {
                if (groupId[0].hasChildNodes()) {
                    groupId[0].childNodes[0].nodeValue = this.deployFile.unitInfo.groupId;
                } else {
                    groupId[0].textContent = this.deployFile.unitInfo.groupId;
                }
            }
            var artifactId = pomXml.getElementsByTagName('artifactId');
            if (artifactId.length) {
                if (artifactId[0].hasChildNodes()) {
                    artifactId[0].childNodes[0].nodeValue = this.deployFile.unitInfo.artifactId;
                } else {
                    artifactId[0].textContent = this.deployFile.unitInfo.artifactId;
                }
            }
            var version = pomXml.getElementsByTagName('version');
            if (version.length) {
                if (version[0].hasChildNodes()) {
                    version[0].childNodes[0].nodeValue = this.deployFile.unitInfo.version;
                } else {
                    version[0].textContent = this.deployFile.unitInfo.version;
                }
            }
            //Serialize updated pom xml back to string and re-set as model
            let backToText = new XMLSerializer();
            this.deployFile.unitConfigFileContent = backToText.serializeToString(pomXml);
        }
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

    /**
     * if debian file upload
     * update path onChange
     */
    updateDebianTargetPath() {
        let path;
        if (this.deployFile.targetPath.indexOf(';') != -1) {
            path = this.deployFile.targetPath.substring(0, this.deployFile.targetPath.indexOf(';'));
        }
        else {
            path = this.deployFile.targetPath;
        }
        let newPath = '';
        newPath += ( path || '');
        if (this.deployFile.unitInfo.distribution) {
            newPath += ";deb.distribution=" + (this.deployFile.unitInfo.distribution || '');
        }
        if (this.deployFile.unitInfo.component) {
            newPath += ";deb.component=" + (this.deployFile.unitInfo.component || '');
        }
        if (this.deployFile.unitInfo.architecture) {
            newPath += ";deb.architecture=" + (this.deployFile.unitInfo.architecture || '');
        }
        this.deployFile.targetPath = '';
        this.deployFile.targetPath = newPath;
    }

    /**
     *onAfterAddingAll Verifies upload only one file
     * @param fileItems
     */
    onAfterAddingAll(fileItems) {
        if (fileItems.length > 1) {
            this.artifactoryNotifications.create({error: "You can only deploy one file"});
            this.deploySingleUploader.clearQueue();
            return;
        }
        let uploadAll = true;

        fileItems.forEach((item)=> {
            if (!item.okToUploadFile) {
                uploadAll = false;
                return;
            }
        });
        if (uploadAll) {
            this.deploySingleUploader.uploadAll();
        }
        else {
            return;
        }
    }

    onAfterAddingFile(fileItem) {
        this.isBundle = _.endsWith(fileItem.file.name, 'zip');
        this.deployFile.fileName = fileItem.file.name;
        if (this.deployFile.targetPath.slice(-1) != "/") {
            this.deployFile.targetPath += "/";
        }
        this.deployFile.targetPath += fileItem.file.name;

        if (fileItem.file.size < 0) {
            fileItem.okToUploadFile = false;
            this.deploySingleUploader.removeFileFromQueue(fileItem);
        }
        else {
            // Save original for display
            fileItem.file.originalName = fileItem.file.name;
            // Encode filename to support UTF-8 strings (server does decode)
            fileItem.file.name = encodeURI(fileItem.file.name);
            fileItem.okToUploadFile = true;
        }
    }

    /**
     * when upload item error push to queue for notifications
     * @param item
     * @param response
     */
    onUploadError(item, response) {
        this.errorQueue.push({item: item, response: response});
        this.artifactoryNotifications.create(response);
        this.deploySingleUploader.removeFileFromQueue(item);
    }

    /**
     * trigger if user checked for edit maven gavc
     * if not set artifactType = 'base'
     */
    changeMavenFileType() {
        if (this.deployFile.unitInfo.artifactType == 'maven') {
            this.deployFile.unitInfo.artifactType = 'base';
            this.deployFile.unitInfo.maven = true;
            if (this.originalDeployPath) {
                this.deployFile.targetPath = angular.copy(this.originalDeployPath);
            }
        }
        else {
            this.deployFile.unitInfo.artifactType = 'maven';
            this.originalDeployPath = angular.copy(this.deployFile.targetPath);
            this.updateMavenTargetPath();
        }
    }

    /**
     * trigger if user checked for edit debian
     * if not set artifactType = 'base'
     */

    changeDebianFileType() {
        if (this.deployFile.unitInfo.artifactType == 'debian') {
            this.deployFile.unitInfo.artifactType = 'base';
            this.deployFile.unitInfo.debian = true;
        }
        else {
            this.deployFile.unitInfo.artifactType = 'debian';
        }
    }

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
            this.deploySingleUploader.clearQueue();
            this.errorQueue = [];
        }
    }

    /**
     * when user removed selected file 'clearPath' is calling
     */
    clearPath() {
        if (this.node.data.isFolder() || this.node.data.isRepo()) {
            this.deployFile.targetPath = this.node.data.path;
        } else {
            this.deployFile.targetPath = this.deployFile.targetPath.replace("/" + this.deployFile.fileName, "");
        }
        this.uploadCompleted = false;
    }

    /**
     *
     * cancel file upload remove from server stock
     */
    onRemoveSingle() {
        if (this.needToCancel) {
            this.artifactDeployDao.cancelUpload({fileName: this.deployFile.fileName});
            this.needToCancel = false;
        }
    }

    /**
     * deploy after adding file to queue
     */
    deployArtifacts() {
        let singleDeploy = {};
        singleDeploy.action = "deploy";
        singleDeploy.unitInfo = this.deployFile.unitInfo;
        singleDeploy.unitInfo.path = angular.copy(this.deployFile.targetPath);
        singleDeploy.fileName = this.deployFile.fileName;
        singleDeploy.repoKey = this.deployFile.repoDeploy.repoKey;

        if (this.deployFile.unitInfo.Internal && this.deployFile.unitConfigFileContent) {
            singleDeploy.publishUnitConfigFile = true;
            singleDeploy.unitConfigFileContent = this.deployFile.unitConfigFileContent;
        }
        if (!this.deployFile.unitInfo.bundle) {
            this.artifactDeployDao.post(singleDeploy).$promise.then((result)=> {
                if (result.data) {
                    this.deploySuccess(result.data);
                }
            });
        }
        else {
            this.artifactDeployDao.postBundle(singleDeploy).$promise.then((result)=> {
                if (result.data) {
                    this.deploySuccess(result.data);
                }
            });
        }
    }

    deploySuccess(data) {
        this.artifactoryNotifications.createMessageWithHtml(this.comm.createNotification(data));
        this.needToCancel = false;
        this.onSuccess();
    }
}
export function jfSingleDeploy() {
    return {
        restrict: 'EA',
        scope: {
            node: '=',
            deploy: '&',
            comm: '=',
            deployFile: '=',
            onSuccess: '&'
        },
        controller: jfSingleDeployController,
        controllerAs: 'jfSingleDeploy',
        bindToController: true,
        templateUrl: 'directives/jf_deploy/jf_single_deploy.html'
    }
}
