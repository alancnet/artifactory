import EVENTS from '../../../../constants/artifacts_events.constants';

class jfXmlViewController {
    constructor($scope, ArtifactViewsDao, ArtifactoryEventBus) {
        this.artifactXmlViewDao = ArtifactViewsDao;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.$scope = $scope;
        this._initXmlView();
    }

    _initXmlView() {
        this._registerEvents();
        this._getXmlViewData();
    }

    _getXmlViewData() {
        this.artifactXmlViewDao.fetch({
            "view": "pom",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
            .then((data) => {
                this.xmlViewData = data;
                this.xmlViewData.fileContent = data.fileContent.trim();
            })
    }

    _registerEvents() {
        let self = this;
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self._getXmlViewData();
            }
        });
    }
}
export function jfXmlView() {
    return {
        restrict: 'EA',
        controller: jfXmlViewController,
        controllerAs: 'jfXmlView',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_xml_view.html'
    }
}