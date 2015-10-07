export function commonGridColumns() {
    return {
        repoPathColumn: function(specialClass) {
            return '<div ng-if="row.entity.repoKey" class="ui-grid-cell-contents '+specialClass+'">{{row.entity.repoKey}}/{{row.entity.path}}</div>' +
                    '<div ng-if="!row.entity.repoKey" class="ui-grid-cell-contents '+specialClass+'">{{row.entity.path}}</div>';
        },

        downloadableColumn: function(specialClass) {
            return '<div ng-if="row.entity.downloadLink"" class="ui-grid-cell-contents '+specialClass+'">{{row.entity.name}}</div>' +
                    '<div ng-if="!row.entity.downloadLink" class="ui-grid-cell-contents '+specialClass+'">{{row.entity.name}}</div>';
        },

        booleanColumn: function(model) {
            return '<div class="grid-checkbox"><input ng-model="' +
                    model + '" type="checkbox" disabled/><span class="icon icon-v"></span></div>';
        },
        checkboxColumn: function(model, click, disabled) {
            return '<div ng-if="!row.entity._emptyRow" class="grid-cell-checkbox"><jf-checkbox><input ng-model="' + model + '"' +
                    (click && click.length ? ' ng-click="' + click + '"' : '') +
                    (disabled && disabled.length ? ' ng-disabled="' + disabled + '"' : '') +
                    ' type="checkbox"/></jf-checkbox></div>';
        }
    }
}