export class AdminRepositoriesLayoutController {

    constructor($scope,$state, ArtifactoryGridFactory, RepositoriesLayoutsDao, uiGridConstants, ArtifactoryFeatures) {
        this.$scope = $scope;
        this.$state = $state;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.layoutsDao = RepositoriesLayoutsDao;
        this.gridOptions = {};
        this.uiGridConstants = uiGridConstants;
        this.enableNew = ArtifactoryFeatures.getCurrentLicense() !== 'OSS';

        this._createGrid();
        this._getLayouts();
    }

    _createGrid() {
        this.gridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this.getColumns())
            .setButtons(this.getActions())
            .setRowTemplate('default');

    }


    _getLayouts() {
        this.layoutsDao.getLayouts().$promise.then((data)=>{
            this.gridOptions.setGridData(data);
        });

    }

    getColumns() {
        return [
            {
                field: "name",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                name: "Name",
                displayName: "Name",
                cellTemplate: '<div class="ui-grid-cell-contents" ui-sref="^.repo_layouts.edit({layoutname: row.entity.name,viewOnly: !row.entity.layoutActions.edit})"><a href="">{{row.entity.name}}</a></div>',
                width: '15%'
            },
            {
                field: "artifactPathPattern",
                name: "Artifact Path Pattern",
                displayName: "Artifact Path Pattern",
                width: '85%'
            }
        ]
    }

    copyLayout(row) {
        this.$state.go('^.repo_layouts.new',{copyFrom: row.name});
    }

    deleteLayout(row) {
        this.layoutsDao.deleteLayout({},{layoutName: row.name}).$promise.then((data)=>{
            this._getLayouts();
        });
    }

    getActions() {
        return [
            {
                icon: 'icon icon-copy',
                tooltip: 'Duplicate',
                callback: (row) => this.copyLayout(row),
                visibleWhen: (row) => row.layoutActions.copy
            },
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: (row) => this.deleteLayout(row),
                visibleWhen: (row) => row.layoutActions.delete
            }

        ];
    }

}
