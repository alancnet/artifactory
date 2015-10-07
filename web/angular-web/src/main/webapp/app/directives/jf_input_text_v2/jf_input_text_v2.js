class jfInputTextV2Controller {

    constructor($element, $scope, $compile,$timeout) {
        this.$element = $element;
        this.$scope = $scope;
        this.$compile = $compile;
        this.$timeout=$timeout;
        this._initInput();

    }

    _initInput() {
        if (this.ismandatory) {
            $(this.$element.find('input')[0]).attr('required','');
            this.$compile(this.$element.find('input'))(this.$scope);
        }
        if (this.autofocus) {
            this.$timeout($(this.$element.find('input')[0]).focus());

        }
    }

    isInputEmpty() {
        return this.model !== "" &&
                this.model !== undefined &&
                this.model !== null;

    }
}

export function jfInputTextV2() {
    return {
        scope: {
            type: '@',
            name: '@',
            text: '@',
            autocomplete:'@',
            autofocus: '=',
            ismandatory: '=',
            model: '=',
            form: '='

        },
        controller: jfInputTextV2Controller,
        controllerAs: 'jfInputTextV2',
        templateUrl: 'directives/jf_input_text_v2/jf_input_text_v2.html',
        terminal:true,
        priority:1000,
        bindToController: true

    }
}