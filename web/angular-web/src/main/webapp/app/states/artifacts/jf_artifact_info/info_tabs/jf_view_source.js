import EVENTS from '../../../../constants/artifacts_events.constants';

class jfViewSourceController {
    constructor($scope, ArtifactViewSourceDao, ArtifactoryEventBus) {
        this.sourceData = '';
        this.artifactViewSourceDao = ArtifactViewSourceDao.getInstance();
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.$scope = $scope;
        this.editorOptions = {
            lineNumbers: true,
            readOnly: 'nocursor',
            lineWrapping: true,
            viewportMargin: Infinity
        };
        this.loadSourceData();
        this._registerEvents();
    }

    loadSourceData() {
        if (this.currentNode.data.mimeType) {
            this.editorOptions.mode = this.currentNode.data.mimeType;
        }
        // get source path from general info
        let sourcePath = _.findWhere(this.currentNode.data.tabs, {name: 'General'}).info.path;
        // fetch source from server
        this.artifactViewSourceDao.fetch({
            "archivePath": this.currentNode.data.archivePath,
            "repoKey": this.currentNode.data.repoKey,
            "sourcePath": sourcePath
        }).$promise
                .then((result) => {
                    this.sourceData = result.source;
                })

    }

    _registerEvents() {
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                this.loadSourceData();
            }
        });
    }
}
export function jfViewSource() {
    return {
        restrict: 'EA',
        scope: {
            currentNode: '='
        },
        controller: jfViewSourceController,
        controllerAs: 'jfViewSource',
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_view_source.html'
    }
}