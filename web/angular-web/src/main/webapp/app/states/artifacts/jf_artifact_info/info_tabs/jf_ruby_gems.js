import EVENTS from '../../../../constants/artifacts_events.constants';
import DICTIONARY from './../../constants/artifact_general.constant';
class jfRubyGemsController {
    constructor($scope, ArtifactViewsDao, ArtifactoryEventBus, ArtifactoryGridFactory) {
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.artifactViewsDao = ArtifactViewsDao;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.DICTIONARY = DICTIONARY.rubyGems;
        this.gemsRubyGridOptions = {};
        this.$scope = $scope;
        this._initRubyGems();
    }

    getRepoPath() {
        return this.currentNode.data.repoKey + "/" + this.currentNode.data.path;
    }

    _initRubyGems() {
        this._createGrid();
        this._registerEvents();
        this._getRubyGemsData();
    }
    _getColumns() {
        return [
            {
                name: 'Name',
                displayName: 'Name',
                field:'name'
            },
            {
                name: 'Version',
                displayName: 'Version',
                field:'version'
            },
            {
                name: 'Type',
                displayName: 'Type',
                field:'type'
            }
        ]
    }

    _getRubyGemsData() {
        this.artifactViewsDao.fetch({
            "view": "gems",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
                .then((data) => {
                    //console.log(data);
                    this.gemsRubyData = data;
                    this.gemsRubyGridOptions.setGridData(this.gemsRubyData.gemsDependencies);
                })
    }

    _createGrid() {
        if (!this.gridFrameworkAssembliesOptions || !Object.keys(this.gridFrameworkAssembliesOptions).length) {
            this.gemsRubyGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                    .setColumns(this._getColumns())
                    .setRowTemplate('default');
        }
        else{
            this._getRubyGemsData();
        }
    }

    _registerEvents() {
        let self = this;
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self._getRubyGemsData();
            }
        });
    }
}
export function jfRubyGems() {
    return {
        restrict: 'EA',
        controller: jfRubyGemsController,
        controllerAs: 'jfRubyGems',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_ruby_gems.html'
    }
}