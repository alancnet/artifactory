import EVENTS from '../../../../constants/artifacts_events.constants';
import DICTIONARY from './../../constants/artifact_general.constant';
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

class jfGeneralController {
    constructor($scope, ArtifactGeneralDao, ArtifactoryNotifications, ArtifactLicensesDao, ChecksumsDao,
            FilteredResourceDao, ArtifactoryEventBus, ArtifactoryModal, DependencyDeclarationDao, $compile, User) {
        this.generalData = {
            dependencyDeclaration: []
        };
        this.$scope = $scope;
        this.artifactLicensesDao = ArtifactLicensesDao;
        this.DICTIONARY = DICTIONARY;
        this.TOOLTIP = TOOLTIP.artifacts.browse;
        this.artifactoryNotifications = ArtifactoryNotifications;
        this.artifactGeneralDao = ArtifactGeneralDao;
        this.filteredResourceDao = FilteredResourceDao;
        this.dependencyDeclarationDao = DependencyDeclarationDao;
        this.modal = ArtifactoryModal;
        this.currentDeclaration = 'Maven';
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.$compile = $compile;
        this.userService = User;
        this.SearchForArchiveLicense = "Search Archive License File";
        this.ChecksumsDao = ChecksumsDao;
        this.editorOptions = {
            lineNumbers: true,
            readOnly: 'nocursor',
            lineWrapping: true,
            height: 'auto',
            mode: 'links',
            mimeType: 'text/xml'
        };
        this._getGeneralData();
        this._registerEvents();
        this._initModalScope();
    }

    _registerEvents() {

        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.showArtifactsCount = false;
                this.calculatingArtifactsCount = false;
                this.finishedArtifactCount = false;
                this.currentNode = node;
                this._getGeneralData();
            }
        });
        this.artifactoryEventBus.registerOnScope(this.$scope, [EVENTS.ACTION_WATCH, EVENTS.ACTION_UNWATCH], () => {
            this._getGeneralData();
        });
    }

    _initModalScope() {
        this.generalScope = this.$scope.$new();
        this.generalScope.closeModal = () => this.modalInstance.close();
        this.generalScope.saveLicenses = (licenses)=>this.saveLicenses(licenses);
        this.generalScope.modalTitle = 'Add Artifactory License Property';
    }

    getGeneralTab() {
        return _.findWhere(this.currentNode.data.tabs, {name: 'General'});
    }

    _getGeneralData() {

        let generalTab = this.getGeneralTab();
        if (generalTab && generalTab.info) { // If general data already exists on the node (for archive children)
            this.generalData = generalTab;
        }
        else if (this.currentNode.data.className === 'TreeNode') {
            this.artifactGeneralDao.fetch({
                "type": this.currentNode.data.type,
                "repoKey": this.currentNode.data.repoKey,
                "path": this.currentNode.data.path
            }).$promise
                    .then((response) => {
                        this.showArtifactsCount = this.artifactsCountEnabled();
                        this.generalData = response;
                        if (this.generalData.dependencyDeclaration) {
                            this.selectDeclaration(this.currentDeclaration);
                        }
                        if (this.generalData.bintrayInfoEnabled) {
                            this.loadPackageDescription();
                        }
                        this.userService.canAnnotate(this.currentNode.data.repoKey,
                                this.currentNode.data.path).then((response) => {
                                    this.canAnnotate = response.data;
                                });
                    })
        }
    }

    calculateArtifactsCount() {
        this.calculatingArtifactsCount = true;
        let {name, repositoryPath} = this.generalData.info;
        this.artifactGeneralDao.artifactsCount({name, repositoryPath}).$promise
                .then((response) => {
                    this.generalData.info.artifactsCount = response.artifactsCount;
                })
                .finally(() => {
                    this.calculatingArtifactsCount = false;
                    this.finishedArtifactCount = true;
                });
    }

    artifactsCountEnabled() {
        return _.contains(['local', 'cached'], this.currentNode.data.repoType);
    }

    onFilteredResourceCB() {
        let payload = {repoKey: this.currentNode.data.repoKey, path: this.currentNode.data.path};
        this.filteredResourceDao.setFiltered({setFiltered: this.generalData.info.filtered},
                payload).$promise.then((res)=> {
                    //console.log(res);
                });
    }

    fixChecksum() {
        this.ChecksumsDao.fix({}, {repoKey: this.currentNode.data.repoKey, path: this.currentNode.data.path})
                .$promise.then((data) => {
                    this._getGeneralData();
                })
    }

    isDeclarationSelected(item) {
        return this.currentDeclaration == item;
    }

    selectDeclaration(item) {
        let self = this;
        this.currentDeclaration = item;
        this.dependencyDeclarationDao.get({
            buildtool: item.toLowerCase(),
            repoKey: this.currentNode.data.repoKey,
            path: this.currentNode.data.path

        }).$promise.then((data)=> {
                    if (data.dependencyData) {
                        self.generalData.dependencyDeclaration.dependencyData = data.dependencyData;
                    }
                });
    }

    loadPackageDescription() {
        this.bintrayData = {};
        this.artifactGeneralDao.bintray({sha1: this.generalData.checksums.sha1Value}).$promise.then((data)=> {
            this.bintrayData = data;
        });
    }

    /**Licenses actions and display
     * saving all default licenses on the generalScope for modal display
     * **/
    openAddLicenseModal() {
        this.modalInstance = this.modal.launchModal('add_license_modal', this.generalScope);
    }

    editLicenses(scan) {
        this.artifactLicensesDao.getLicenses().$promise.then((licenses)=> {
            this.generalScope.licenses = _.map(licenses, (rec)=> {
                return rec.name
            });
            if (!scan) {
                this.generalScope.selectedLicenses = this.generalData.info.licenses;
            }
            this.openAddLicenseModal();
        })
    }

    saveLicenses(selectedLicenses) {
        this.artifactLicensesDao.setLicenses({
            repoKey: this.currentNode.data.repoKey,
            path: this.currentNode.data.path
        }, selectedLicenses).$promise.then((result)=> {
                    this._getGeneralData();
                    this.modalInstance.close();
                });
    }

    deleteLicenses() {
        this.modal.confirm("Are you sure you want to delete the license information attached to " + this.generalData.info.name + "?")
                .then(() => {
                    this.saveLicenses([]);
                });
    }

    scanForLicenses() {
        this.artifactLicensesDao.scanArtifact({
            repoKey: this.currentNode.data.repoKey,
            path: this.currentNode.data.path
        }).$promise.then((result)=> {
                    if (result.data.length > 0) {
                        this.generalScope.selectedLicenses = _.map(result.data, (rec)=> {
                            return rec.name
                        });
                        this.editLicenses(true)
                    }
                    else {
                        this.artifactoryNotifications.create({info: 'No licenses found in scan'})
                    }
                });
    }

    searchForArchiveFile() {
        this.artifactLicensesDao.getArchiveLicenseFile({
            repoKey: this.currentNode.data.repoKey,
            path: this.currentNode.data.path
        }).$promise.then((data)=> {
                    this.modal.launchCodeModal('License File', data.data);
                }, ()=> {
                    this.SearchForArchiveLicense = "(No archive license file found)";
                });

    }

    queryCodeCenter() {
        this.artifactLicensesDao.queryCodeCenter({
            repoKey: this.currentNode.data.repoKey,
            path: this.currentNode.data.path
        }).$promise.then((result)=> {
                    this._getGeneralData();
                });
    }

}

export function jfGeneral() {
    return {
        restrict: 'EA',
        scope: {
            currentNode: '='
        },
        controller: jfGeneralController,
        controllerAs: 'jfGeneral',
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_general.html'
    }
}