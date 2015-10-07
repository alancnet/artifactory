class jfCodeController {

    constructor($element) {

    }
}

export function jfCode () {
    return {
        restrict: 'EA',
        scope: {
            src: '='
        },
        controller: jfCodeController,
        controllerAs: 'jfCode',
        bindToController: true,
        template: '<pre>{{jfCode.src | json}}</pre>'
    }
}

