import EVENTS from '../../../../constants/artifacts_events.constants';
import DICTIONARY from './../../constants/artifact_general.constant';

class jfDockerV2Controller {
    constructor($scope, ArtifactViewsDao, ArtifactoryEventBus) {
        this.$scope = $scope;
        this.artifactViewsDao = ArtifactViewsDao;
        this.DICTIONARY = DICTIONARY.dockerV2;
        this.artifactoryEventBus = ArtifactoryEventBus;

        this.dockerV2Data = null;

        this._getDockerV2Data();
        this._registerEvents();

    }

    _registerEvents() {
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            this.currentNode = node;
            this._getDockerV2Data();
        });
    }

    _getDockerV2Data() {
        this.artifactViewsDao.fetch({
            "view": "dockerv2",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise.then((data) => {
                    this.dockerV2Data = data;
                    if (this.layersController)
                        this.layersController.refreshView();
                });
    }

    isNotEmptyValue(value) {
        return value && (!_.isArray(value)  || value.length>0);
    }

    formatValue(value) {
        if (_.isArray(value)) {
            return value.join(', ');
        }
        else return value;
    }
}
export function jfDockerV2() {
    return {
        restrict: 'EA',
        controller: jfDockerV2Controller,
        controllerAs: 'jfDockerV2',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_docker_v2.html'
    }
}