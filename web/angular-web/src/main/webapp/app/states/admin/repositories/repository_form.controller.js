import fieldsValuesDictionary from '../../../constants/field_options.constats';
import TOOLTIP from '../../../constants/artifact_tooltip.constant';

export class AdminRepositoryFormController {
    constructor($q, $scope, $stateParams, $state, RepositoriesDao, PropertySetsDao, ArtifactoryGridFactory,
            ArtifactoryModal, ArtifactoryFeatures, ArtifactoryNotifications, commonGridColumns) {
        this.$scope = $scope;
        this.$q = $q;
        this.currentTab = 'basic';
        this.$stateParams = $stateParams;
        this.propertySetsDao = PropertySetsDao;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.commonGridColumns = commonGridColumns;
        this.notifications = ArtifactoryNotifications;
        this.modal = ArtifactoryModal;
        this.$state = $state;
        this.repositoriesDao = RepositoriesDao;
        this.newRepository = false;
        this.features = ArtifactoryFeatures;
        this.replicationsGridOption = {};
        this.replicationScope = $scope.$new();
        this.TOOLTIP = TOOLTIP.admin.repositories;

        this._createGrid();
        this.initRepoForm();
        this.repoType = this.$stateParams.repoType;
        if (this.$stateParams.repoKey) {
            this.title = "Edit " + this.$stateParams.repoKey + " Repository";
            this.newRepository = false;
            this.editRepository(this.$stateParams.repoKey);
        }
        else {
            this.newRepository = true;
            this.repoInfo = new RepositoriesDao();
            this.title = "New " + _.capitalize(this.repoType) + " Repository";
            this._initNewRepositoryTypeConfig();

            if (this.repoType == fieldsValuesDictionary.REPO_TYPE.REMOTE) {
                if (!this.repoInfo.basic) {
                    this.repoInfo.basic = {};
                    this.repoInfo.basic.contentSynchronisation = {};
                    this.repoInfo.basic.contentSynchronisation.statistics = {};
                    this.repoInfo.basic.contentSynchronisation.properties = {};
                }

                this.repoInfo.basic.contentSynchronisation.enabled = false;
                this.repoInfo.basic.contentSynchronisation.statistics.enabled = false;
                this.repoInfo.basic.contentSynchronisation.properties.enabled = false;
            }
        }
        this.packageType = fieldsValuesDictionary.repoPackageTypes;
    }

    isCurrentRepoType(type) {
        return this.repoType == type;
    }

    /**
     * init propertiesSets  and replication scope functions for modal and fields options
     */
    initRepoForm() {
        this.replicationScope.replication = {}; //to create a single replication
        this.replicationScope.testLocalReplicationUrl = (url)=>this.testLocalReplicationUrl(url);

        this.replicationScope.addReplication = (replication)=> this.addReplication(replication);
        this.replicationScope.closeModal = ()=>this.closeModal();
    }

    /**
     * run only if edit repository and get repository data
     */
    editRepository(repoKey) {
        this.repositoriesDao.getRepository({type: this.repoType, repoKey: repoKey}).$promise
                .then(info => {
                    this.repoInfo = info;
                    //console.log(info);
                    if (this.repoInfo.replications && this.repoInfo.replications.length) {
                        this.repoInfo.cronExp = this.repoInfo.replications[0].cronExp;
                        this.repoInfo.enableEventReplication = this.repoInfo.replications[0].enableEventReplication;
                    }
                    if (this.repoType.toLowerCase() == fieldsValuesDictionary.REPO_TYPE.VIRTUAL) {
                        this._getRepositoriesByType();
                    }
                    if (this.repoType == fieldsValuesDictionary.REPO_TYPE.REMOTE && this.repoInfo.replications) {
                        this.repoInfo.replication = this.repoInfo.replications[0];
                    }
                    else {
                        this.replicationsGridOption.setGridData(this.repoInfo.replications);
                    }
                    this._getFieldsOptions();

                    this.lastSmartRemoteURL = this.repoInfo.basic.url;
                    if (this.repoType == fieldsValuesDictionary.REPO_TYPE.REMOTE && !this.repoInfo.basic.contentSynchronisation.enabled)
                        this._detectSmartRepository();
                });

    }

    _setDefaultProxy() {
        if (this.newRepository && this.fields.defaultProxy && _.has(this.repoInfo, 'advanced.network')) {
            !this.repoInfo.advanced.network.proxy ?
                    this.repoInfo.advanced.network.proxy = this.fields.defaultProxy : '';
        }
    }

    /**
     *test button  when adding new replication in local repository
     */
    testLocalReplicationUrl(url) {
        // Create a copy of the repo
        let testRepo = angular.copy(this.repoInfo);

        // Make sure replications is not null
        testRepo.replications = testRepo.replications || [];

        let testReplication;
        if (this.replicationScope.sourceReplication) {
            testReplication = _.findWhere(testRepo.replications, {url: this.replicationScope.sourceReplication.url});
            angular.copy(this.replicationScope.replication, testReplication);
        }
        else {
            testReplication = angular.copy(this.replicationScope.replication);
            testRepo.replications.push(testReplication);
        }

        testReplication.cronExp = this.repoInfo.cronExp;
        testReplication.nextTime = this.repoInfo.nextTime;
        testReplication.type = this.repoType;
        testReplication.enableEventReplication = this.repoInfo.enableEventReplication;

        this.repositoriesDao.testLocalReplication({replicationUrl: url}, testRepo);
    }

    testRemoteUrl() {
        this.repositoriesDao.testRemoteUrl(this.repoInfo).$promise.then((result)=> {
            //console.log(result);
        });

        this._detectSmartRepository();
    }

    _detectSmartRepository() {

        if (this.features.isOss()) {
            return this.$q.when();
        }

        let defer = this.$q.defer();

        this.repositoriesDao.detectSmartRepository(this.repoInfo).$promise.then((result)=> {
            if (result.info == 'true') {
                if (!this.repoInfo.basic.contentSynchronisation.enabled || this.repoInfo.basic.url != this.lastSmartRemoteURL) {
                    this.repoInfo.basic.contentSynchronisation.enabled = true;
                    this.lastSmartRemoteURL = this.repoInfo.basic.url;

                    let modalInstance;
                    let modalScope = this.$scope.$new();
                    modalScope.smartRepo = this.repoInfo.basic.contentSynchronisation;
                    modalScope.smartRepo.typeSpecific = this.repoInfo.typeSpecific;
                    modalScope.closeModal = () => modalInstance.close();
                    modalInstance = this.modal.launchModal('smart_remote_repository', modalScope);

                    defer.resolve(true);
                }
                else
                    defer.resolve(false);
            }
            else {
                this.repoInfo.basic.contentSynchronisation.enabled = false;
                defer.resolve(false);
            }
        });

        return defer.promise;
    }

    testRemoteReplication() {
        this.addReplication(this.repoInfo.replication);
        this.repositoriesDao.testRemoteReplication(this.repoInfo).$promise.then((result)=> {
            //            console.log(result);
        });
    }

    setSnapshotVersionBehavior() {
        if (this.repoInfo && this.repoInfo.typeSpecific && this.repoInfo.typeSpecific.snapshotVersionBehavior) {
            if (this.repoInfo.typeSpecific.snapshotVersionBehavior == 'NONUNIQUE') {
                this.repoInfo.typeSpecific.maxUniqueSnapshots = 0;
                this.disableMaxUniqueSnapshots = true;
            }
            else {
                this.disableMaxUniqueSnapshots = false;
            }
        }
    }

    _initNewRepositoryTypeConfig() {
        if (this.repoType == fieldsValuesDictionary.REPO_TYPE.LOCAL) {
            this.repoInfo.type = 'localRepoConfig';
        }
        if (this.repoType == fieldsValuesDictionary.REPO_TYPE.REMOTE) {
            this.repoInfo.type = 'remoteRepoConfig';
        }
        if (this.repoType == fieldsValuesDictionary.REPO_TYPE.VIRTUAL) {
            this.repoInfo.type = 'virtualRepoConfig';
        }

        this._getDefaultModels()
                .then(()=> {
                    this._getFieldsOptions()
                            .then(()=> {
                                this._setDefaultFields();
                                this.openRepoTypeModal();
                            });
                });
    }

    /**
     * get all properties
     */
    _populateProperties() {
        return this.propertySetsDao.query({isRepoForm: true}).$promise.then((properites)=> {
            this.propertiesList = properites;
        });
    }

    /**
     * set dropdown options and default fields
     */
    _getFieldsOptions() {

        return this.repositoriesDao.getAvailableChoicesOptions().$promise.then((fields)=> {
            this.fields = fields;
            this.localChecksumPolicies = fieldsValuesDictionary['localChecksumPolicy'];
            this.localChecksumPoliciesKeys = Object.keys(this.localChecksumPolicies);
            this.remoteChecksumPolicies = fieldsValuesDictionary['remoteChecksumPolicy'];
            this.remoteChecksumPoliciesKeys = Object.keys(fieldsValuesDictionary['remoteChecksumPolicy']);
            fields.proxies = fields.proxies || [];
            fields.proxies.unshift('');
            this.repositoryLayouts = fields.repositoryLayouts;
            this.remoteLayoutMapping = angular.copy(fields.repositoryLayouts);
            this.remoteLayoutMapping.unshift('');
            this.mavenSnapshotRepositoryBehaviors = fieldsValuesDictionary['snapshotRepositoryBehavior'];
            this.mavenSnapshotRepositoryBehaviorsKeys = Object.keys(fieldsValuesDictionary['snapshotRepositoryBehavior']);
            this.pomCleanupPolicies = fieldsValuesDictionary['pomCleanupPolicy'];
            this.pomCleanupPoliciesKeys = Object.keys(fieldsValuesDictionary['pomCleanupPolicy']);
            this.vcsGitProviderOptions = fieldsValuesDictionary['vcsGitProvider'];
            this.vcsGitProviderOptionsKeys = Object.keys(fieldsValuesDictionary['vcsGitProvider']);
            this.setSnapshotVersionBehavior();
            return this._populateProperties();
        });
    }

    /**
     * fetching from server the default data
     */
    _getDefaultModels() {
        return this.repositoriesDao.getDefaultValues().$promise.then((models)=> {
            this.defaultModels = models.defaultModels;

        });

    }


    /**
     * check and set current tab
     */
    setCurrentTab(tab) {
        if (this.features.isDisabled(tab)) {
            return;
        }
        this.currentTab = tab;
    }

    isCurrentTab(tab) {
        return this.currentTab === tab;
    }

    /**
     * handle save or update click
     */
    save() {
        if (this.repoType == fieldsValuesDictionary.REPO_TYPE.LOCAL) {
            if (this.repoInfo.replications && this.repoInfo.replications.length) {
                this.saveCronAndEventFlagToAllReplicationsAndValidateHa();
            }
            //Warn user if saving cron expression without any replication config
            if (this.repoInfo.cronExp && (!this.repoInfo.replications || !this.repoInfo.replications.length)) {
                this.notifications.create({warn: 'A cron expression was entered without any replication configuration.'
                + '\nThe expression will not be saved.'
                });
            }
        }

        if (this.repoType == fieldsValuesDictionary.REPO_TYPE.REMOTE) {
            this._detectSmartRepository().then((result) => {
                if (!result) {
                    //Add replication if exists:
                    if (this.repoInfo.replication) {
                        if (this.repoInfo.cronExp && this.repoInfo.replication.enabled
                                && (!this.repoInfo.advanced.network.username || !this.repoInfo.advanced.network.password)) {
                            this.notifications.create({error: 'Pull replication requires non-anonymous authentication to the ' +
                            'remote repository.\nPlease make sure to fill the \'Username\' and \'Password\' fields in the '
                            + 'Advanced settings tab or remove the fields you filled in the replication tab.'});
                            return false;
                        }
                        this.addReplication(this.repoInfo.replication);
                    }
                    if (this.repoInfo.advanced.network.proxy === '') {
                        delete this.repoInfo.advanced.network.proxy;
                    }

                    this.save_update();
                }
            });

            return false;
        }
        else
            this.save_update();
    }

    /**
     * save or update wizard form
     */
    save_update() {
        if (this.newRepository) {
            this.repositoriesDao.save(this.repoInfo).$promise.then((result)=> {
                this.$state.go('^.list', {repoType: this.repoType});
            });
        } else {
            this.repositoriesDao.update(this.repoInfo).$promise.then((result)=> {
                this.$state.go('^.list', {repoType: this.repoType});
            });
        }
    }

    /**
     * button pre and  forward at the bottom page
     */
    prevStep() {
        if (this.currentTab == 'advanced') {
            this.setCurrentTab('basic');
        }
        else if (this.currentTab == 'replications') {
            this.setCurrentTab('advanced');
        }
    }

    fwdStep() {
        if (this.currentTab == 'basic') {
            this.setCurrentTab('advanced');
            return;
        }
        if (this.currentTab == 'advanced' && this.repoType != fieldsValuesDictionary.REPO_TYPE.VIRTUAL) {
            this.setCurrentTab('replications');
        }
    }

    /**
     * function for select package type
     */
    openRepoTypeModal() {
        this.$repoTypeScope = this.$scope.$new();
        this.$repoTypeScope.packageTypes = this.getPackageType();
        this.$repoTypeScope.closeModal = () =>  this.closeModalPackageType();
        this.$repoTypeScope.modalClose = ()=> this.modalClose();
        this.$repoTypeScope.selectRepoType = (type)=>this.selectRepoType(type);
        this.isTypeModalOpen = true;

        this.repoTypeModal = this.modal.launchModal('repository_type_modal', this.$repoTypeScope)
        this.repoTypeModal.result.finally(() => this.isTypeModalOpen = false);
    }

    closeModalPackageType() {
        if (!this.repoType) {
            return false;
        }
        if (this.newRepository) {
            this.setRepoLayout();
        }
        if (this.repoType.toLowerCase() == fieldsValuesDictionary.REPO_TYPE.VIRTUAL) {
            // Resetting resolved and selected repositories lists in case we are changing package type
            if (this.newRepository) {
                this.repoInfo.basic.selectedRepositories = [];
                this.repoInfo.basic.resolvedRepositories = [];
            }
            this._getRepositoriesByType();
        }
    }

    _getRepositoriesByType() {
        this.repositoriesDao.availableRepositoriesByType({
            type: this.repoInfo.typeSpecific.repoType,
            repoKey: this.repoInfo.general ? this.repoInfo.general.repoKey : ''
        }).$promise.then((repos)=> {
                    repos.availableLocalRepos = _.map(repos.availableLocalRepos, (repo)=> {
                        return {
                            repoName: repo,
                            type: 'local',
                            _iconClass: "icon icon-local-repo"
                        }
                    });
                    repos.availableRemoteRepos = _.map(repos.availableRemoteRepos, (repo)=> {
                        return {
                            repoName: repo,
                            type: 'remote',
                            _iconClass: "icon icon-remote-repo"
                        };
                    });
                    repos.availableVirtualRepos = _.map(repos.availableVirtualRepos, (repo)=> {
                        return {
                            repoName: repo,
                            type: 'virtual',
                            _iconClass: "icon icon-virtual-repo"
                        };
                    });

                    this.repoInfo.basic.selectedRepositories = _.map(this.repoInfo.basic.selectedRepositories,
                            (repo)=> {
                                if (repo.type == 'local') {
                                    return {
                                        repoName: repo.repoName,
                                        type: 'local',
                                        _iconClass: "icon icon-local-repo"
                                    }
                                }
                                else if (repo.type == 'remote') {
                                    return {
                                        repoName: repo.repoName,
                                        type: 'remote',
                                        _iconClass: "icon icon-remote-repo"
                                    }
                                }
                                else if (repo.type == 'virtual') {
                                    return {
                                        repoName: repo.repoName,
                                        type: 'virtual',
                                        _iconClass: "icon icon-virtual-repo"
                                    }
                                }
                            });


                    this.repositoriesList = [];
                    this.repositoriesList = repos.availableLocalRepos.concat(repos.availableRemoteRepos).concat(repos.availableVirtualRepos);
                });
    }

    getReplicationActions() {
        return [
            {
                icon: 'icon icon-run',
                tooltip: 'Run Now',
                visibleWhen: row => row.enabled,
                callback: row => this.executeReplicationNow(row)
            },
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: row => this._deleteReplication(row)
            }
        ]
    }

    executeReplicationNow(row) {
        if (true) {
            this.repositoriesDao.executeReplicationNow({replicationUrl: row.url},
                    this.repoInfo).$promise.then((result)=> {
                        //console.log(result)
                    });
        }
    }

    setRepoLayout() {
        let foundLayout = false;
        if (_.has(this.repoInfo, 'typeSpecific.repoType')) {
            let type = this.repoInfo.typeSpecific.repoType.toLowerCase();
            let defaultLayouts = fieldsValuesDictionary['defaultLayouts'];
            if (!this.repoInfo.basic) {
                this.repoInfo.basic = {};
                this.repoInfo.basic.repositoryLayout = {};
            }
            if (this.repoType.toLowerCase() == fieldsValuesDictionary.REPO_TYPE.VIRTUAL) {
                this.repoInfo.basic.layout = "";
                foundLayout = true;
            } else {
                let defaultLayout = defaultLayouts[type];
                if (defaultLayout && _.includes(this.repositoryLayouts, defaultLayout)) {
                    this.repoInfo.basic.layout = defaultLayout;
                    foundLayout = true;
                } else {
                    this.repositoryLayouts.forEach((layout)=> {
                        if (layout.indexOf(type) != -1) {
                            this.repoInfo.basic.layout = layout;
                            foundLayout = true;
                        }
                    });
                }
                if (!foundLayout) {
                    this.repoInfo.basic.layout = "simple-default";
                }
            }
        }
    }

    /**
     * set default fields for new repository
     */
    _setDefaultValuesByType() {
        if (!(this.repoInfo && this.repoInfo.typeSpecific)) {
            this.repoInfo.typeSpecific = {};
        }
        let type = this.repoInfo.typeSpecific.repoType.toLowerCase();
        if (type && this.defaultModels[type]) {
            angular.extend(this.repoInfo.typeSpecific, this.defaultModels[type]);
            // add default remote url for remote repository
            if (this.repoType.toLocaleLowerCase() == fieldsValuesDictionary.REPO_TYPE.REMOTE) {
                this.repoInfo.basic.url = this.defaultModels[type].url;
            }
        }
        if (this.repoType.toLowerCase() == fieldsValuesDictionary.REPO_TYPE.VIRTUAL) {
            this.repoInfo.basic.repositoryLayout = '';
        }
    }

    _setDefaultFields() {
        if (!this.repoInfo.typeSpecific) {
            this.repoInfo.typeSpecific = {};
        }
        this.repoInfo.advanced = {};
        this.repoInfo.advanced.cache = {};
        this.repoInfo.advanced.network = {};
        angular.extend(this.repoInfo.advanced.cache, this.defaultModels['cache']);
        angular.extend(this.repoInfo.advanced.network, this.defaultModels['network']);


        if (this.repoType == fieldsValuesDictionary.REPO_TYPE.REMOTE) {
            if (!this.repoInfo.advanced) {
                this.repoInfo.advanced = {};
            }
            if (!this.repoInfo.basic) {
                this.repoInfo.basic = {};
            }

            angular.extend(this.repoInfo.advanced, this.defaultModels['remoteAdvanced']);
            angular.extend(this.repoInfo.basic, this.defaultModels['remoteBasic']);
        }
        else if (this.repoType == fieldsValuesDictionary.REPO_TYPE.LOCAL || this.repoType == fieldsValuesDictionary.REPO_TYPE.VIRTUAL) {
            if (!this.repoInfo.advanced) {
                this.repoInfo.advanced = {};
            }
            if (!this.repoInfo.basic) {
                this.repoInfo.basic = {};
            }
            angular.extend(this.repoInfo.advanced, this.defaultModels['localAdvanced']);
            angular.extend(this.repoInfo.basic, this.defaultModels['localBasic']);
            this.repoInfo.typeSpecific.localChecksumPolicy = this.defaultModels['maven'].localChecksumPolicy;
        }
        this._setDefaultProxy();

    }

    selectRepoType(type) {
        if (this.features.isDisabled(type.value)) {
            return;
        }

        this.repoTypeModal.close();
        if (!this.repoInfo.typeSpecific) {
            this.repoInfo.typeSpecific = {};
        }
        this.repoInfo.typeSpecific.repoType = type.serverEnumName;

        if (this.newRepository) {
            this._setDefaultValuesByType();
        }
        this.closeModalPackageType();
    }

    /**
     * newReplication; editReplication->
     * functions for replications modal (work only for local repos)
     */
    newReplication() {
        if (this.repoInfo.replications && this.repoInfo.replications.length && this.features.isDisabled('highAvailability')) {
            this.notifications.create({warn: 'Multi-push replication will only work with an Enterprise license'});
            return true;
        }
        this.replicationScope.replication = {};
        this.replicationScope.title = 'New Replication';
        this.replicationScope.replication.socketTimeout = 15000;
        this.replicationScope.replication.syncProperties = true;
        this.replicationScope.sourceReplication = null;
        this.replicationScope.replication.enabled = true;
        this.replicationModal(false);
    }


    editReplication(row) {
        this.replicationScope.title = 'Replication Properties';
        this.replicationScope.replication = angular.copy(row);
        this.replicationScope.sourceReplication = row;
        this.replicationModal(true);
    }

    _deleteReplication(row) {
        _.remove(this.repoInfo.replications, row);
        this.replicationsGridOption.setGridData(this.repoInfo.replications);

    }

    replicationModal(isEdit) {
        this.replicationScope.replication.proxies = this.fields.proxies;
        if(!isEdit) {
            this.fields.defaultProxy ? this.replicationScope.replication.proxy = this.fields.defaultProxy : '';
        }
        this.modalInstance = this.modal.launchModal('replication_modal', this.replicationScope);
    }

    /**
     * add replication: function that save fields in form for replication.
     * if local: push it for grid replication
     * if remote: clear exsit replication and set the new one
     */
    addReplication(replication) {

        if (this.repoType.toLowerCase() == fieldsValuesDictionary.REPO_TYPE.REMOTE) {
            this.repoInfo.replications = [];
        }
        replication.enabled = replication.enabled ? replication.enabled : false;
        replication.syncDeletes = replication.syncDeletes ? replication.syncDeletes : false;
        replication.syncProperties = replication.syncProperties ? replication.syncProperties : false;
        replication.cronExp = this.repoInfo.cronExp;
        replication.nextTime = this.repoInfo.nextTime;
        replication.enableEventReplication = this.repoInfo.enableEventReplication;
        replication.type = this.repoType;
        if (replication.proxy === '') {
            delete replication.proxy;
        }
        if (this.replicationScope.sourceReplication) {
            // updating replication
            angular.copy(replication, this.replicationScope.sourceReplication);
        } else {
            // adding new replication
            this.repoInfo.replications = this.repoInfo.replications || [];
            this.repoInfo.replications.push(replication);
        }
        if (this.repoType.toLocaleLowerCase() == fieldsValuesDictionary.REPO_TYPE.LOCAL) {
            this.replicationsGridOption.setGridData(this.repoInfo.replications);
            this.closeModal();
        }
    }

    /**
     * Saves the cron expression and event replication flag to all replications.
     * Also validates that if HA license is not installed - only one active replication is saved.
     */
    saveCronAndEventFlagToAllReplicationsAndValidateHa() {
        //Signifies save should disable all replications but one because multiple enabled replicaions exist without HA license
        let notHa = this.features.isDisabled('highAvailability') && this.repoInfo.replications.length > 1;
        this.repoInfo.replications.forEach((replication) => {
            replication.cronExp = this.repoInfo.cronExp;
            replication.enableEventReplication = this.repoInfo.enableEventReplication;
            if(notHa) {
                replication.enabled = false;
            }
        });
        if(notHa) {
            this.notifications.create({warn: 'You saved multiple enabled replication configurations.\n Multi-push ' +
            'replication is only available with an Enterprise licenses therefore only the first replication will be' +
            'saved as enabled and the rest will be disabled.'});
            this.repoInfo.replications[0].enabled = true;
        }
    }

    closeModal() {
        this.modalInstance.close();
    }

    _createGrid() {
        this.replicationsGridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getColumns())
                .setRowTemplate('default')
                .setButtons(this.getReplicationActions())
                .setGridData([]);
    }

    /**
     * controller display arrows form
     */
    showNextButton() {
        if (this.repoType == fieldsValuesDictionary.REPO_TYPE.LOCAL || this.repoType == fieldsValuesDictionary.REPO_TYPE.REMOTE) {
            if (this.features.isDisabled('replications')) {
                return this.currentTab != 'advanced';
            }
            return this.currentTab != 'replications';
        }
        else {
            return this.currentTab != 'advanced';
        }
    }

    _getColumns() {
        return [
            {
                name: 'URL',
                displayName: 'URL',
                field: 'url',
                cellTemplate: '<div class="ui-grid-cell-contents"><a ng-click="grid.appScope.RepositoryForm.editReplication(row.entity)">{{row.entity.url}}</a></div>'

            },
            {
                name: 'Sync Deletes',
                displayName: 'Sync Deletes',
                field: 'syncDeletes',
                cellTemplate: this.commonGridColumns.checkboxColumn('row.entity.syncDeletes')
            },
            {
                name: 'Sync Properties',
                displayName: 'Sync Properties',
                field: 'syncProperties',
                cellTemplate: this.commonGridColumns.checkboxColumn('row.entity.syncProperties')
            },
            {
                name: 'Enabled',
                displayName: 'Enabled',
                field: 'enabled',
                cellTemplate: this.commonGridColumns.checkboxColumn('row.entity.enabled')
            }
        ]
    }

    /**
     * all packages sorts by type
     */
    getPackageType() {
        switch (this.repoType) {
            case fieldsValuesDictionary.REPO_TYPE.LOCAL:
            {
                return _.filter(this.packageType,(type) => {
                   return _.indexOf(type.repoType, fieldsValuesDictionary.REPO_TYPE.LOCAL) != -1});
            }
            case fieldsValuesDictionary.REPO_TYPE.REMOTE:
            {
                return _.select(this.packageType,(type) => {
                    return _.indexOf(type.repoType, fieldsValuesDictionary.REPO_TYPE.REMOTE) != -1});
            }
            case fieldsValuesDictionary.REPO_TYPE.VIRTUAL:
            {
                return _.select(this.packageType,(type) => {
                    return _.indexOf(type.repoType, fieldsValuesDictionary.REPO_TYPE.VIRTUAL) != -1});
            }
        }
    }

    cancel() {
        this.modal.confirm("You have unsaved changes. Leaving this page will discard changes.", "Discard Changes", { confirm: "Discard" }).then(()=> {
                    this.$state.go('^.list', {repoType: this.repoType});
                });

    }
}