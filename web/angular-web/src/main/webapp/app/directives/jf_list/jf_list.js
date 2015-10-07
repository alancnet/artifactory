class jfListController {
    constructor() {
       // this.selectedItems = [];
    }

    isSelected(item) {
        return this._inSelectedItems(item) > -1;
    }

    toggleSelection(item) {
        let index = this._inSelectedItems(item);

        if (index > -1) {
            this.selectedItems.splice(index, 1);
        } else {
            this.selectedItems.push(item);
        }
        this.getSelectedItems();
    }

    _inSelectedItems(item) {
        return this.selectedItems.indexOf(item);
    }

    getSelectedItems() {

        this.selectedItemsList({list: this.selectedItems});

    }

}

export function jfList() {
    return {
        scope: {
            list: '=',
            selectedItems: '='
        },
        controller: jfListController,
        controllerAs: 'jfList',
        templateUrl: 'directives/jf_list/jf_list.html',
        bindToController: true
    }
}