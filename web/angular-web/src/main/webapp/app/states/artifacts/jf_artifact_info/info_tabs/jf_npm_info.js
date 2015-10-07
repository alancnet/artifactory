import EVENTS from '../../../../constants/artifacts_events.constants';
import DICTIONARY from './../../constants/artifact_general.constant';
class jfNpmInfoController {
    constructor($scope, ArtifactViewsDao, ArtifactoryEventBus, ArtifactoryGridFactory) {
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.artifactViewsDao = ArtifactViewsDao;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.DICTIONARY = DICTIONARY.npm;
        this.npmGridOptions = {};
        this.$scope = $scope;

        this._initNpmInfo();
    }

    _initNpmInfo() {
        this._createGrid();
        this._registerEvents();
        this._getNpmInfoData();
    }

    _getNpmInfoData() {
        this.artifactViewsDao.fetch({
            "view": "npm",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
                .then((data) => {
                    this.npmData = data;
                    this.npmGridOptions.setGridData(this.npmData.npmDependencies || []);
                })
    }

    _createGrid() {
        if (!Object.keys(this.npmGridOptions).length) {
            this.npmGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                    .setRowTemplate('default');
        }
    }

    _registerEvents() {
        let self = this;
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self._getNpmInfoData();
            }
        });
    }

}

export function jfNpmInfo() {
    return {
        restrict: 'EA',
        controller: jfNpmInfoController,
        controllerAs: 'jfNpmInfo',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_npm_info.html'
    }
}