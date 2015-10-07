import EVENTS from '../../../../constants/artifacts_events.constants';
import DICTIONARY from './../../constants/artifact_general.constant';
class jfNugetController {
    constructor($scope, ArtifactViewsDao, ArtifactoryEventBus, ArtifactoryGridFactory) {
        this.artifactViewsDao = ArtifactViewsDao;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.DICTIONARY = DICTIONARY.nuget;
        this.gridDependenciesOptions = {};
        this.gridFrameworkAssembliesOptions = {};
        this.nugetData = {};
        this.$scope = $scope;
        this._initNuget();
    }

    _initNuget() {
        this._registerEvents();
        this.getNugetData();
    }

    getNugetData() {
        this.artifactViewsDao.fetch({
            "view": "nuget",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
                .then((data) => {
                    this.nugetData = data;
                        this._createGrid();
                });
    }

    _createGrid() {
        if (this.nugetData.dependencies) {
            if (!Object.keys(this.gridDependenciesOptions).length) {
                this.gridDependenciesOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                        .setRowTemplate('default')
                        .setColumns(this._getColumns('dependencies'))
                        .setGridData(this.nugetData.dependencies)
            }
            else {
                this.gridDependenciesOptions.setGridData(this.nugetData.dependencies)
            }
        }
        if (this.nugetData.frameworkAssemblies) {
            if (!Object.keys(this.gridFrameworkAssembliesOptions).length) {
                this.gridFrameworkAssembliesOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                        .setColumns(this._getColumns('frameworkAssemblies'))
                        .setRowTemplate('default')
                        .setGridData(this.nugetData.frameworkAssemblies)
            }
            else {
                this.gridFrameworkAssembliesOptions.setGridData(this.nugetData.frameworkAssemblies);
            }
        }
    }

    _getColumns(gridType) {
        if (gridType === 'dependencies') {
            return [{
                name: 'Id',
                displayName: 'Id',
                field: 'id'
            },
                {
                    name: 'Version',
                    displayName: 'Version',
                    field: 'version'
                },
                {
                    name: 'Target Framework',
                    displayName: 'Target Framework',
                    field: 'targetFramework'
                }]
        }
        if (gridType === 'frameworkAssemblies') {
            return [
                {
                    name: 'Assembly Name',
                    displayName: 'Assembly Name',
                    field: 'assemblyName'
                },
                {
                    name: 'Target Framework',
                    displayName: 'Target Framework',
                    field: 'targetFramework'
                }
            ]
        }
    }

    _registerEvents() {
        let self = this;

        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self.getNugetData();
            }
        });
    }

}
export function jfNuget() {
    return {
        restrict: 'EA',
        controller: jfNugetController,
        controllerAs: 'jfNuget',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_nuget.html'
    }
}