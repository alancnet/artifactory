import EVENTS from '../../../../constants/artifacts_events.constants';
import DICTIONARY from './../../constants/artifact_general.constant';

class jfDockerAncestryController {
    constructor($scope, ArtifactViewsDao, ArtifactoryEventBus, $q) {
        this.artifactViewsDao = ArtifactViewsDao;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.DICTIONARY = DICTIONARY.dockerAncestry;
        this.dockerAncestryData = {};
        this.$scope = $scope;
        this.$q = $q;
        this._initDockerAncestry();
    }

    _initDockerAncestry() {
        this._registerEvents();
        this.getDockerAncestryData();
    }

    getDockerAncestryData() {
        this._findAncestryJsonNode()
            .then((node) => {
                return this.artifactViewsDao.fetch({
                    "view": "dockerancestry",
                    "repoKey": node.repoKey,
                    "path": node.path
                }).$promise;
            })
            .then((data) => {
                this.dockerAncestryData = this._linkedListToArray(data.dockerLinkedImage);
            });
    }


    _findAncestryJsonNode() {
        return this.currentNode.data.getChildren()
        .then((data) => {
            for (var i=0; i<data.length;i++) {
                if (data[i].text === 'ancestry.json') {
                    return data[i];
                }
            }
            return this.$q.reject();
        });
    }

    gotoPath(index) {
        var repoKey = this.currentNode.data.repoKey;
        var fullpath = repoKey + '/' + this.dockerAncestryData[index].path;

        this.artifactoryEventBus.dispatch(EVENTS.TREE_NODE_OPEN, fullpath);
    }

    _linkedListToArray(linkedData) {
        var arr = [];
        var curr = linkedData;
        var indent = 1;
        while (curr) {
            var rec = {id: curr.id,
                       size: curr.size,
                       path: curr.path,
                       indent: '|' + '__'.repeat(indent)};
            arr.push(rec);
            curr = curr.child;
            indent++;
        }
        return  arr;
    }

    _registerEvents() {
        let self = this;

        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self.getDockerAncestryData();
            }
        });
    }

}
export function jfDockerAncestry() {
    return {
        restrict: 'EA',
        controller: jfDockerAncestryController,
        controllerAs: 'jfDockerAncestry',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_docker_ancestry.html'
    }
}