import EVENTS from '../../../../constants/artifacts_events.constants';
import DICTIONARY from './../../constants/artifact_general.constant';
class jfRpmController {
    constructor($scope, ArtifactViewsDao, ArtifactoryEventBus, ArtifactoryGridFactory) {
        this.artifactViewsDao = ArtifactViewsDao;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.DICTIONARY = DICTIONARY.rpm;
        this.gridProvideOptions = {};
        this.gridRequireOptions = {};
        this.gridObsoleteOptions = {};
        this.gridConflictOptions = {};
        this.rpmData = {};
        this.$scope = $scope;
        this._initRpm();
    }

    _initRpm() {
        this._registerEvents();
        this.getRpmData();
    }

    getRpmData() {
        this.artifactViewsDao.fetch({
            "view": "rpm",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
                .then((data) => {
                    this.rpmData = data;
                    this._createGrid();
                });
    }

    _createGrid() {
        if (this.rpmData.provide) {
            if (!Object.keys(this.gridProvideOptions).length) {
                this.gridProvideOptions = this.artifactoryGridFactory.getGridInstance()
                        .setRowTemplate('default')
                        .setColumns(this._getColumns('provide'))
                        .setGridData(this.rpmData.provide)
            }
            else {
                this.gridProvideOptions.setGridData(this.rpmData.provide)
            }
        }
        if (this.rpmData.require) {
            if (!Object.keys(this.gridRequireOptions).length) {
                this.gridRequireOptions = this.artifactoryGridFactory.getGridInstance()
                        .setColumns(this._getColumns())
                        .setRowTemplate('default')
                        .setGridData(this.rpmData.require)
            }
            else {
                this.gridRequireOptions.setGridData(this.rpmData.require);
            }
        }
        if (this.rpmData.obsolete) {
            if (!Object.keys(this.gridObsoleteOptions).length) {
                this.gridObsoleteOptions = this.artifactoryGridFactory.getGridInstance()
                        .setColumns(this._getColumns())
                        .setRowTemplate('default')
                        .setGridData(this.rpmData.obsolete)
            }
            else {
                this.gridObsoleteOptions.setGridData(this.rpmData.obsolete);
            }
        }

        if (this.rpmData.conflict) {
            if (!Object.keys(this.gridConflictOptions).length) {
                this.gridConflictOptions = this.artifactoryGridFactory.getGridInstance()
                        .setColumns(this._getColumns())
                        .setRowTemplate('default')
                        .setGridData(this.rpmData.conflict)
            }
            else {
                this.gridConflictOptions.setGridData(this.rpmData.conflict);
            }
        }
    }

    _getColumns() {
        return [
            {
                name: 'Name',
                displayName: 'Name',
                field: 'name',
                width: '25%'
            },
            {
                name: 'Flags',
                displayName: 'Flags',
                field: 'flags',
                width: '15%'
            },
            {
                name: 'Epoch',
                displayName: 'Epoch',
                field: 'epoch',
                width: '15%'
            },
            {
                name: 'Version',
                displayName: 'Version',
                field: 'version',
                width: '15%'
            }, {
                name: 'Release',
                displayName: 'Release',
                field: 'release',
                width: '15%'
            },
            {
                name: 'Pre',
                displayName: 'Pre',
                field: 'pre',
                width: '15%'
            }]
    }

    _registerEvents() {
        let self = this;

        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self.getRpmData();
            }
        });
    }

}
export function jfRpm() {
    return {
        restrict: 'EA',
        controller: jfRpmController,
        controllerAs: 'jfRpm',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_rpm_info.html'
    }
}