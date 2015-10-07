import EVENTS from '../../../../constants/artifacts_events.constants';

class jfPomViewController {
    constructor($scope, ArtifactViewsDao, ArtifactoryEventBus) {
        this.artifactPomViewDao = ArtifactViewsDao;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.$scope = $scope;
        this._initPomView();
    }

    _initPomView() {
        this._registerEvents();
        this._getPomViewData();
    }

    _getPomViewData() {
        this.artifactPomViewDao.fetch({
            "view": "pom",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
                .then((data) => {
                    this.pomViewData= data;
                    this.pomViewData.fileContent=data.fileContent.trim();
                })
    }

    _registerEvents() {
        let self = this;
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self._getPomViewData();
            }
        });
    }
}
export function jfPomView() {
    return {
        restrict: 'EA',
        controller: jfPomViewController,
        controllerAs: 'jfPomView',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_pom_view.html'
    }
}