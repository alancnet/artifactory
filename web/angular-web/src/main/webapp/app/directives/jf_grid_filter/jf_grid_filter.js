/*
 USAGE EXAMPLE:

 <jf-grid-filter
 filter-grid="gridOptions"  //the name of the grid (grid options)
 filter-field="fieldName"        //the name of the field that should be filtered
 filter-on-change>          //optional - don't use a button for filtering, filter on every change
 </jf-grid-filter>

 */


class jfGridFilterController {

    constructor($scope,$timeout) {

        this.$timeout = $timeout;
        this.gridFilter = '';

        this.grid.enableFiltering = true;

        var cols = this.grid.columnDefs;
        this.column = _.find(cols, _.matchesProperty('field', this.filterField));
        if (this.filterField2) this.column2 = _.find(cols, _.matchesProperty('field', this.filterField2));

        $scope.$on('$destroy', () => this.onDestroy());
    }

    shouldFilterOnChange() {
        return this.filterOnChange !== 'false';
    }

    doFilter() {
        if (!this.column) return;
        if (!this.column2) {
//            this.column.filter = {term: '*' + this.gridFilter + '*'};
            this.column.filter = {
                term: '*' + this.gridFilter + '*',
                condition: (searchTerm, cellValue, row, column)=> {
                    let regex = new RegExp('.*' + searchTerm.split('\\*').join('.*') + '.*', "i");
                    return regex.test(cellValue) || row.entity._emptyRow;
                }
            };
        }
        else {
            this.column.filter = {
                term: '*' + this.gridFilter + '*',
                condition: (searchTerm, cellValue, row, column)=> {
                    let cell2Value = row.entity[this.column2.field];
                    let regex = new RegExp('.*' + searchTerm.split('\\*').join('.*') + '.*', "i");
                    return regex.test(cellValue) || regex.test(cell2Value)  || row.entity._emptyRow;
                }
            };
        }
        this._refreshGrid();
    }

    onChange() {
        if (this.shouldFilterOnChange()) this.doFilter();
    }

    onDestroy() {
        this.gridFilter = '';
        this.doFilter();
    }

    _refreshGrid() {
        //yes, it's so very ugly... but nothing else worked...
        //also, it should be a method of ArtifactoryGrid...

        this.column.name += ' ';
        if (this.column2) this.column2.name += ' ';
        var data = [];
        if (this.grid.data.length) {
            data[0] = _.cloneDeep(this.grid.data[0]);
            data = data.concat(this.grid.data.slice(1));
        }

        this.grid.setGridData(data);
        this.$timeout(()=>{
            if (this.grid.api.grid.getVisibleRows().length === 0) {
                this.grid.setGridData(data.concat([{_emptyRow:true}]));
            }
            else if (this.grid.api.grid.getVisibleRows().length > 1) {
                this.grid.setGridData(_.filter(data,(row)=>{
                    return !row._emptyRow;
                }));
            }
            this.$timeout(()=> {
                this.grid.api.core.refresh();
            });
        });

    }

    getPlaceHolder() {
        if (this.column2) {
            return 'Filter by ' + (this.column.displayName || this.column.name) + ' or ' + (this.column2.displayName || this.column2.name);
        }
        else {
            return 'Filter by ' + (this.column.displayName || this.column.name);
        }
    }
}

export function jfGridFilter() {

    return {
        restrict: 'E',
        scope: {
            disableButton: '=',
            filterField: '@',
            filterField2: '@',
            grid: '=filterGrid',
            filterOnChange: '@',
            autoFocus: '@'
        },
        controller: jfGridFilterController,
        controllerAs: 'jfGridFilter',
        templateUrl: 'directives/jf_grid_filter/jf_grid_filter.html',
        bindToController: true
    };
}
