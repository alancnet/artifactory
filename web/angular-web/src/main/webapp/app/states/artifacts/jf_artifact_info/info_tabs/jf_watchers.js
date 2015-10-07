import EVENTS from '../../../../constants/artifacts_events.constants';

class jfWatchersController {

    constructor($scope, $state, ArtifactWatchesDao, ArtifactoryGridFactory, ArtifactoryEventBus, $q,
            ArtifactoryStorage) {
        this.$scope = $scope;
        this.$state = $state;
        this.watchersGridOption = {};
        this.artifactWatchesDao = ArtifactWatchesDao;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.artifactoryStorage = ArtifactoryStorage;
        this.$q = $q;
        this._createGrid();
        this._getWatchesData();
        this._registerEvents();
    }

    _registerEvents() {
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            this.currentNode = node;
            this._getWatchesData();
        });
        this.artifactoryEventBus.registerOnScope(this.$scope, [EVENTS.ACTION_WATCH, EVENTS.ACTION_UNWATCH], () => {
            this._getWatchesData();
        });
    }

    _deleteWatches(watches) {
        let data = watches.map((watch) => {
            let selectedWachers = {
                name: watch.watcherName,
                repoKey: watch.watchConfigureOn.split(':')[0],
                path: watch.watchConfigureOn.split(':')[1]
            }
            return selectedWachers;
        });
        let json = {watches: data};

        return this.artifactWatchesDao.delete(json).$promise
                .then(() => {
                    this.artifactoryEventBus.dispatch(EVENTS.ACTION_UNWATCH, this.currentNode);
                    this._getWatchesData();
                });
    }

    _createGrid() {
        let batchActions = [{
            callback: (watches) => this._deleteWatches(watches),
            name: "Delete",
            icon: 'clear'
        }];

        this.watchersGridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getColumns())
                .setRowTemplate('default')
                .setMultiSelect()
                .setBatchActions(batchActions)
                .setButtons(this._getActions());

    }

    _getColumns() {
        return [
            {
                name: "Watcher Name",
                displayName: "Watcher Name",
                field: "watcherName",
                width: '20%'
            },
            {
                name: "Watching Since",
                displayName: "Watching Since",
                field: "watchingSince",
                width: '30%'
            },
            {
                name: "Watch Configured On",
                displayName: "Watch Configured On",
                field: 'watchConfigureOn',
                width: '50%'
            }

        ]
    }

    showInTree(row) {
        let browser = this.artifactoryStorage.getItem('BROWSER') || 'tree';
        let repoKey = row.watchConfigureOn.split(':')[0];
        let path = row.watchConfigureOn.split(':')[1];
        let artifactPath = repoKey + "/" + (path);
        let archivePath = '';
        this.$state.go('artifacts.browsers.path', {
            "tab": "General",
            "browser": browser,
            "artifact": artifactPath
        });
    }

    _getActions() {
        return [{
            icon: 'icon icon-show-in-tree',
            tooltip: 'Show In Tree',
            callback: row => this.showInTree(row)
        },
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: (watch) => this._deleteWatches([watch])
            }];
    }

    _getWatchesData() {
        let self = this;
        this.artifactWatchesDao.query({
            path: self.currentNode.data.path,
            repoKey: self.currentNode.data.repoKey
        }).$promise.then((watchers) => {
                    this.watchers = watchers;
                    this.watchersGridOption.setGridData(watchers);
                });
    }

}
export function jfWatchers() {
    return {
        restrict: 'EA',
        scope: {
            currentNode: '='
        },
        controller: jfWatchersController,
        controllerAs: 'jfWatchers',
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_watchers.html'
    }
}