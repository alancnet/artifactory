import EVENTS from '../../../../constants/artifacts_events.constants';
import DICTIONARY from './../../constants/artifact_general.constant';
class jfBowerController {
    constructor($scope, ArtifactViewsDao, ArtifactoryEventBus, ArtifactoryGridFactory) {
        this.artifactViewsDao = ArtifactViewsDao;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.DICTIONARY = DICTIONARY.bower;
        this.gridDependenciesOptions = {};
        this.bowerData = {};
        this.$scope = $scope;
        this._initBower();
    }

    _initBower() {
        this._registerEvents();
        this.getBowerData();
    }

    getBowerData() {
        this.artifactViewsDao.fetch({
            "view": "bower",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
                .then((data) => {
                    this.bowerData = data;
                    this._createGrid();
                });
    }

    _createGrid() {
        if (this.bowerData.bowerDependencies) {
            if (!Object.keys(this.gridDependenciesOptions).length) {
                this.gridDependenciesOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                        .setRowTemplate('default')
                        .setColumns(this._getColumns())
                        .setGridData(this.bowerData.bowerDependencies)
            }
            else {
                this.gridDependenciesOptions.setGridData(this.bowerData.bowerDependencies)
            }
        }
    }

    _getColumns() {
        return [{
            name: 'Name',
            displayName: 'Name',
            field: 'name'
        },
        {
            name: 'Version',
            displayName: 'Version',
            field: 'version'
        }];
    }

    _registerEvents() {
        let self = this;

        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self.getBowerData();
            }
        });
    }

}
export function jfBower() {
    return {
        restrict: 'EA',
        controller: jfBowerController,
        controllerAs: 'jfBower',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_bower.html'
    }
}