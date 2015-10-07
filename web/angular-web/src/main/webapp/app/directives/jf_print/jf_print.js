export function jfPrint () {
    return {
        restrict: 'E',
        scope: {
            content: '@'
        },
        controller: jfPrintController,
        controllerAs: 'jfPrint',
        bindToController: true,
        templateUrl: 'directives/jf_print/jf_print.html'
    }
}


class jfPrintController {

    constructor($element, $window, $scope) {
        this.$element = $element;
        this.$window = $window;
        this.$scope = $scope;

        this._registerEvents();
    }

    _registerEvents() {
        this.$element.on('click', () => this.print());
        this.$scope.$on('$destroy', () => this.$element.off('click'));
    }

    print() {
        let printWindow = this.$window.open('', '_blank', 'height=380,width=750');
        printWindow.document.write('<html><head><title>Artifactory</title></head><body >');
        printWindow.document.write('<pre>' + this._escapeHTML(this.content) + '</pre>');
        printWindow.document.write('</body></html>');
        printWindow.print();
        printWindow.close();
        return true;
    }

    _escapeHTML(content) {
        let escape = document.createElement('textarea');
        escape.innerHTML = content;
        return escape.innerHTML;
    }


}
