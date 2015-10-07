import TOOLTIP from '../../constants/artifact_tooltip.constant';

export class PushToBintrayModal {
    constructor($stateParams, $rootScope, $q, ArtifactoryModal, PushToBintrayDao, ArtifactoryNotifications) {

        this.ArtifactoryNotifications = ArtifactoryNotifications;
        this.$rootScope = $rootScope;
        this.$stateParams = $stateParams;
        this.$q = $q;
        this.modal = ArtifactoryModal;
        this.ptbDao = PushToBintrayDao;
    }

    _getBuildBintrayRepositories() {
        this.ptbDao.getBuildRepos().$promise.then((data) => {
            this.modalScope.data.bintrayRepos = _.map(data.binTrayRepositories,(repo) => {return {text:repo ,value: repo}});

        })
            .catch((err) => {
                if (err.data && err.data.feedbackMsg && err.data.feedbackMsg.error) {
                    let msg = err.data.feedbackMsg.error;
                    this.ArtifactoryNotifications.create({error: msg});
                }
            });

    }

    _getBuildBintrayPackages() {
        this.ptbDao.getBuildPacks({key: this.modalScope.selection.bintrayRepo}).$promise.then((data) => {
            data.binTrayPackages = _.filter(data.binTrayPackages,(pack) => {return pack!=='_'});
            this.modalScope.data.bintrayPackages = _.map(data.binTrayPackages,(pack) => {return {text:pack ,value: pack}});
            if (data.binTrayPackages && data.binTrayPackages.length) {
                if (!this.modalScope.selection.bintrayPackageName) this.modalScope.selection.bintrayPackageName = data.binTrayPackages[0];
            }
            if (this.modalScope.selection.bintrayPackageName) {
                this._getBuildBintrayVersions();
            }
            else {
                this.modalScope.data.bintrayPackageVersions = [{text:'1.0' ,value: '1.0'}];
                this.modalScope.selection.bintrayPackageVersion = '1.0';
            }
        });
    }

    _getBuildBintrayVersions() {
        this.ptbDao.getBuildVersions({
            key: this.modalScope.selection.bintrayRepo,
            id: this.modalScope.selection.bintrayPackageName
        }).$promise.then((data) => {
                    this.modalScope.data.bintrayPackageVersions = _.map(data.binTrayVersions,(ver) => {return {text:ver ,value: ver}});
                    if (data.binTrayVersions && data.binTrayVersions.length && !this.modalScope.selection.bintrayPackageVersion) {
                        this.modalScope.selection.bintrayPackageVersion = data.binTrayVersions[0];
                    }

                })
        .catch(()=>{
                    this.modalScope.data.bintrayPackageVersions = [{text:'1.0' ,value: '1.0'}];
                    this.modalScope.selection.bintrayPackageVersion = '1.0';
                })
    }

    _pushBuildToBintray(backgroundPush) {

        let payload = {

            buildName: this.$stateParams.buildName,
            buildNumber: this.$stateParams.buildNumber,
            buildTime: this.$stateParams.startTime,
            bintrayParams: {
                useExistingProps: this.modalScope.selection.useSpecificProperties,
                notify: this.modalScope.selection.sendEmail,
                repo: this.modalScope.selection.bintrayRepo,
                packageId: this.modalScope.selection.bintrayPackageName,
                version: this.modalScope.selection.bintrayPackageVersion
            }
        };

        this.ptbDao.pushBuildToBintray({background: backgroundPush}, payload).$promise.then((response)=> {
            this.createPushToBintrayResponse(response);
        }).finally(() => this.modalInstance.close());
    }

    _pushArtifactToBintray() {

        let payload = {
            bintrayParams: this.bintrayParams
        };

        payload.bintrayParams.repo = this.modalScope.selection.bintrayRepo;
        payload.bintrayParams.packageId = this.modalScope.selection.bintrayPackageName;
        payload.bintrayParams.version = this.modalScope.selection.bintrayPackageVersion;
        payload.bintrayParams.path = this.modalScope.selection.filePath;

        this.ptbDao.pushArtifactToBintray({
            repoKey: this.params.repoKey,
            path: this.params.path
        }, payload).$promise.then((response)=> {
                this.createPushToBintrayResponse(response);
            }).finally(() => this.modalInstance.close());

    }


    _getArtifactBintrayData() {

        this.ptbDao.getArtifactData({repoKey: this.params.repoKey, path: this.params.path}).$promise.then((data) => {
            this.bintrayParams = data.bintrayParams;
            this.modalScope.selection.bintrayRepo = data.bintrayParams.repo;
            this.modalScope.selection.bintrayPackageName = data.bintrayParams.packageId;
            this.modalScope.selection.filePath = data.bintrayParams.path;
            this.modalScope.selection.bintrayPackageVersion = data.bintrayParams.version;

            this.modalScope.data.bintrayRepos = _.map(data.binTrayRepositories, (repo) => {
                return {text: repo, value: repo}
            });//data.binTrayRepositories;
            if (data.bintrayParams.packageId) this.modalScope.data.bintrayPackages = [data.bintrayParams.packageId];
            if (data.bintrayParams.version) this.modalScope.data.bintrayPackageVersions = [{
                text: data.bintrayParams.version,
                value: data.bintrayParams.version
            }];

            if (this.modalScope.selection.bintrayRepo) this._getBuildBintrayPackages();
        })
            .catch((err) => {
                if (err.data && err.data.feedbackMsg && err.data.feedbackMsg.error) {
                    let msg = err.data.feedbackMsg.error;
                    this.ArtifactoryNotifications.create({error: msg});
                }
            });
    }


    launchModal(type, params) {


        let deferred = this.$q.defer();
        this.modalScope = this.$rootScope.$new();
        this.modalScope.selection = {};
        this.modalScope.data = {};
        this.modalScope.tooltip = TOOLTIP.artifacts.pushToBintray;

        this.modalScope.cancel = () => {
            this.modalInstance.close();
            deferred.reject();
        };

        this.modalScope.onRepoSelect = () => {
            this._getBuildBintrayPackages();
        };
        this.modalScope.onPackageSelect = () => {
            this._getBuildBintrayVersions();
        };

        this.modalScope.selectizeConfig = {
            sortField: 'number',
            create: true,
            maxItems: 1
        };


        this.modalScope.pushType = type;

        if (type === 'build') {
            this.modalScope.push = () => {
                this._pushBuildToBintray(false);
            };

            this.modalScope.backgroundPush = () => {

                this._pushBuildToBintray(true);
            };

            this._getBuildBintrayRepositories();
        }
        else if (type === 'artifact') {
            this.modalScope.push = () => {
                this._pushArtifactToBintray(false);
            };

            let repoPath = params.repoPath;
            let arr = repoPath.split(':');
            let repoKey = arr[0];
            let path = arr[1];

            this.params = {repoKey: repoKey, path: path};

            this._getArtifactBintrayData();
        }
        this.modalInstance = this.modal.launchModal("push_to_bintray", this.modalScope);

        return deferred.promise;

    }

    createPushToBintrayResponse(response) {
        if (response.data.error) {
            this.createPushToBintrayErrorResponse(response.data);
            return;
        }
        let artifactBintrayUrl = response.data.url;
        if (artifactBintrayUrl) {
            this.ArtifactoryNotifications.createMessageWithHtml({
                type: 'success',
                body: `${response.data.info} <a href="${artifactBintrayUrl}" target="_blank">${artifactBintrayUrl}</a>`
            });
        }
    }

    createPushToBintrayErrorResponse(response) {
        if (response.error) {
            this.ArtifactoryNotifications.create(response);
        }
    }
}