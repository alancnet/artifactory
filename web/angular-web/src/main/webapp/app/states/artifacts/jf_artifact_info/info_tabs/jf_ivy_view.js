import EVENTS from '../../../../constants/artifacts_events.constants';

class jfIvyViewController {
    constructor($scope, ArtifactViewsDao, ArtifactoryEventBus) {
        this.artifactIvyViewDao = ArtifactViewsDao;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.$scope = $scope;
        this._initIvyView();
    }

    _initIvyView() {
        this._registerEvents();
        this._getIvyViewData();
    }

    _getIvyViewData() {
        this.artifactIvyViewDao.fetch({
            "view": "pom",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
                .then((data) => {
                    //console.log(data);
                    this.ivyViewData= data;
                    this.ivyViewData.fileContent=data.fileContent.trim();
                })
    }

    _registerEvents() {
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                this._getIvyViewData();
            }
        });
    }
}
export function jfIvyView() {
    return {
        restrict: 'EA',
        controller: jfIvyViewController,
        controllerAs: 'jfIvyView',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_ivy_view.html'
    }
}