import EVENTS from '../../../../constants/artifacts_events.constants';
import DICTIONARY from './../../constants/artifact_general.constant';

class jfDockerController {
    constructor($scope, ArtifactViewsDao, ArtifactoryEventBus) {
        this.artifactViewsDao = ArtifactViewsDao;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.DICTIONARY = DICTIONARY.docker;
        this.dockerData = {};
        this.$scope = $scope;
        this._initDocker();
    }

    _initDocker() {
        this._registerEvents();
        this.getDockerData();
    }

    gotoPath(key) {
        var repoKey = this.currentNode.data.repoKey;
        var pathField = key === 'imageId' ? 'imageIdPath' : (key === 'parent' ? 'parentIdPath' : undefined);
        if (pathField) {
            this.artifactoryEventBus.dispatch(EVENTS.TREE_NODE_OPEN,
                                              repoKey + '/' + this.dockerData.dockerInfo[pathField]);
        }
    }

    getDockerData() {
        this.artifactViewsDao.fetch({
            "view": "docker",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path + '/json.json'
        }).$promise.then((data) => {
            this.dockerData = data;
        });
    }

    _registerEvents() {
        let self = this;

        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self.getDockerData();
            }
        });
    }



}
export function jfDocker() {
    return {
        restrict: 'EA',
        controller: jfDockerController,
        controllerAs: 'jfDocker',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_docker.html'
    }
}