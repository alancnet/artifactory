import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class AdminAdvancedStorageSummaryController {
    constructor($scope,StorageSummaryDao, ArtifactoryGridFactory, uiGridConstants) {
        this.$scope = $scope;
        this.storageSummary = {};
        this.gridOption = {};
        this.uiGridConstants = uiGridConstants;
        this.storageSummaryDao = StorageSummaryDao.getInstance();
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.TOOLTIP = TOOLTIP.admin.advanced.storageSummary;
        this.storageSummaryDao.get().$promise.then((result) => {
            this.storageSummary = result;
            this.storageSummary.repositoriesSummaryList = _.map(this.storageSummary.repositoriesSummaryList,(row)=>{
                row.usedSpace = {value: row.usedSpace, isTotal: row.repoKey==='TOTAL'};
                return row;
            });

            this.createGrid();
        });
    }

    createGrid() {
        this.gridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this.getColumns())
                .setGridData(this.storageSummary.repositoriesSummaryList)
                .setRowTemplate('default');
    }

    sortByteSizes(a,b) {

        let res = 0;
        if (a===undefined || b===undefined) return res;

        if (!a.isTotal && !b.isTotal) {
            var gb = [a.value.match('GB'), b.value.match('GB')],
                    mb = [a.value.match('MB'), b.value.match('MB')],
                    kb = [a.value.match('KB'), b.value.match('KB')]

            res = (gb[0] && !gb[1]) ? 1 : (gb[1] && !gb[0]) ? -1 :
                      (mb[0] && !mb[1]) ? 1 : (mb[1] && !mb[0]) ? -1 :
                      (kb[0] && !kb[1]) ? 1 : (kb[1] && !kb[0]) ? -1 :
                      (parseFloat(a.value.match(/[+-]?\d+(\.\d+)?/)[0]) > parseFloat(b.value.match(/[+-]?\d+(\.\d+)?/)[0])) ? 1 : -1
        }
        else if (a.isTotal) res = -1;
        else if (b.isTotal) res = 1;

        return res;
    }

    getColumns() {
        return [
            {
                field: "repoKey",
                name: "Repository Key",
                displayName: "Repository Key"
            },
            {
                field: "repoType",
                name: "Repository Type",
                displayName: "Repository Type"
            },
            {
                field: "packageType",
                name: "Package Type",
                displayName: "Package Type"
            },
            {
                field: "percentage",
                cellTemplate: '<div class="ui-grid-cell-contents text-center">{{row.entity.displayPercentage}}</div>',
                name: "Percentage",
                displayName: "Percentage"
            },
            {
                field: "usedSpace",
                name: "Used Space",
                displayName: "Used Space",
                cellTemplate: '<div class="ui-grid-cell-contents text-center">{{row.entity.usedSpace.value}}</div>',
                sortingAlgorithm : this.sortByteSizes,
                sort: {
                    direction: this.uiGridConstants.DESC
                }

            },
            {
                field: "filesCount",
                name: "Files",
                displayName: "Files"
            },
            {
                field: "foldersCount",
                name: "Folders",
                displayName: "Folders"
            },


            {
                field: "itemsCount",
                name: "Items",
                displayName: "Items"
            },

        ]
    }


}