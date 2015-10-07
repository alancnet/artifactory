export function jfGrid($timeout,$compile) {

    let isListTooltip = (cell) => {
        let parent = cell.context.parentElement;
        return (parent.classList.contains('tooltip-show-list'));
    };

    let formatListContent = (content)=>{
        let pipeIndex = content.indexOf('|');
        if (pipeIndex>=0) {
            let listContent = content.substr(pipeIndex+1);
            let list = listContent.split(',');
            let cleanList = _.map(list,(line)=>{
                return line.trim();
            });
            return cleanList.join('\n');
        }
        else return content;
    };

    return {
        scope: {
            gridOptions: '=',
            filterField: '@?',
            filterField2: '@?',
            filterOnChange: '@?',
            autoFocus: '@',
            objectName: '@'
        },
        templateUrl: 'directives/jf_grid/jf_grid.html',
        link: ($scope, $element, $attrs) => {

            $scope.noCount = $attrs.hasOwnProperty('noCount');
            $scope.noPagination = $attrs.hasOwnProperty('noPagination');

            $($element).on('mouseenter', '.ui-grid-cell, .ui-grid-cell-contents, .btn-action', (e)=>{
                let cellItem = $(e.target);

                cellItem.parents('.ui-grid-row').addClass('hovered');
                $scope.$apply();

                if (cellItem.hasClass('ui-grid-cell-contents')) {
                    let cellItemContent = cellItem.text().trim();

                    if (cellItemContent.length > 0 && cellItem[0].scrollWidth > cellItem.innerWidth()) {
                        if (!cellItem.hasClass('tooltipstered')) {
                            cellItem.tooltipster({
                                trigger: 'hover',
                                onlyOne: 'true',
                                interactive: 'true',
                                position: 'bottom',
                                content: isListTooltip(cellItem) ? formatListContent(cellItemContent) : cellItemContent
                            });
                            cellItem.tooltipster('show');
                        }
                        else {
                            cellItem.tooltipster('enable');

                            let currentContent = isListTooltip(cellItem) ? formatListContent(cellItemContent) : cellItemContent;
                            if (cellItem.tooltipster('content') != currentContent)
                                cellItem.tooltipster('content', currentContent);
                        }
                    }
                    else if (cellItem.hasClass('tooltipstered'))
                        cellItem.tooltipster('disable');
                }
            }).on('mouseleave', '.ui-grid-draggable-row, .ui-grid-cell, .ui-grid-cell-contents, .btn-action', (e)=>{
                let currentRowElement = $(e.currentTarget).parents('.ui-grid-row'),
                        toRowElement = $(e.relatedTarget).parents('.ui-grid-row');

                if (!toRowElement || !currentRowElement.is(toRowElement)) {
                    currentRowElement.removeClass('hovered');
                    $scope.$apply();
                }
            });
            $scope.$on('$destroy', () => {
                $($element).off('mouseenter');
                $($element).off('mouseleave');
            });


            $scope.getTotalRecords = () => {
                let count;

                if (!$scope.gridOptions.api) return 0;

                let visRows = $scope.gridOptions.api.grid.getVisibleRows();
                let totalRows = $scope.gridOptions.api.grid.rows.length;
                if (_.findWhere(visRows,{entity:{_emptyRow:true}}))
                    count = totalRows - 1;
                else
                    count = totalRows;

                let recordsName;

                if ($scope.objectName) {
                    if ($scope.objectName.indexOf('/')>=0) {
                        let splited = $scope.objectName.split('/');
                        recordsName = count !== 1 ? splited[1] : splited[0];
                    }
                    else
                        recordsName = count !== 1 ? $scope.objectName + 's' : $scope.objectName;
                }
                else
                    recordsName = count !== 1 ? 'records' : 'record';

                return count + ' ' + _.startCase(recordsName);
            };
        }
    }
}
