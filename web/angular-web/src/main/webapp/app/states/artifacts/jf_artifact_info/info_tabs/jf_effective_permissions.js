import EVENTS from '../../../../constants/artifacts_events.constants';

class jfEffectivePermissionsController {
    constructor(ArtifactoryGridFactory, ArtifactPermissionsDao, $scope, $timeout, ArtifactoryEventBus,
            uiGridConstants, commonGridColumns) {
        this.uiGridConstants = uiGridConstants;
        this.commonGridColumns = commonGridColumns;
        this.$scope = $scope;
        this.$timeout = $timeout;
        this.permissionGridOption = {};
        this.permissionsDao = ArtifactPermissionsDao.getInstance();
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.artifactoryEventBus=ArtifactoryEventBus;
        this._registerEvents();
        this._createGrid();
        this._getPermissionsData();
    }

    _getPermissionsData() {
        return this.permissionsDao.query({
            path: this.currentNode.data.path,
            repoKey: this.currentNode.data.repoKey,
            pageNum: 1,
            numOfRows: 25,
            direction: "asc",
            orderBy: "principal"
        }).$promise.then((data)=>{
                   this.permissionGridOption.setGridData(data.pagingData);
                });
    }

    _createGrid() {
        this.permissionGridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this.getColumns())
                .setRowTemplate('default');
    }
    _registerEvents() {
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            this.permissionGridOption.resetPagination();
            this.permissionGridOption.getPage();
            this.$timeout(()=>{
                this._getPermissionsData();
            });
        });
    }
    getColumns() {
        return [
            {
                name: 'Principal',
                displayName: 'Principal',
                field: "principal",
                cellTemplate: '<div class="ui-grid-cell-contents">{{COL_FIELD CUSTOM_FILTERS}}</div>',
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                width: '33%'
            },
            {
                name: 'Type',
                displayName: 'Type',
                field: "type",
                cellTemplate: '<div class="ui-grid-cell-contents">{{COL_FIELD CUSTOM_FILTERS}}</div>',
                width: '8%'
            },
            {
                name: "Delete/Overwrite",
                displayName: "Delete/Overwrite",
                field: "permission.delete",
                cellTemplate: this.commonGridColumns.booleanColumn('MODEL_COL_FIELD'),
                width: '19%'
            },
            {
                name: "Deploy/Cache",
                displayName: "Deploy/Cache",
                field: "permission.deploy",
                cellTemplate: this.commonGridColumns.booleanColumn('MODEL_COL_FIELD'),
                width: '17%'
            },
            {
                name: "Annotate",
                displayName: "Annotate",
                field: "permission.annotate",
                cellTemplate: this.commonGridColumns.booleanColumn('MODEL_COL_FIELD'),
                width: '15%'
            },
            {
                name: "Read",
                displayName: "Read",
                field: "permission.read",
                cellTemplate: this.commonGridColumns.booleanColumn('MODEL_COL_FIELD'),
                width: '8%'
            }
        ]
    }

}
export function jfEffectivePermissions() {
    return {
        restrict: 'EA',
        controller: jfEffectivePermissionsController,
        controllerAs: 'jfEffectivePermissions',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_effective_permissions.html'
    }
}