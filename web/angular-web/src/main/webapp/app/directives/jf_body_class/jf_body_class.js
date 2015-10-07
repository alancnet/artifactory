export function jfBodyClass() {
    return {
        restrict: 'A',
        controller: jfBodyClassController,
        controllerAs: 'jfBodyClass',
        bindToController: true
    }
}

class jfBodyClassController {
    constructor($element, $state, $scope) {
        this.$element = $element;
        this.$state = $state;
        this.$scope = $scope;

        this._registerEvents();
    }

    _registerEvents() {
        this.$scope.$on('$stateChangeSuccess', () => {
            this._setBodyClass()
        });
    }

    _setBodyClass() {
        this.$element.attr('class', this._formatCssClass(this.$state.$current.name));
    }

    _formatCssClass(stateName) {
        return stateName.replace(/\./g, '-');
    }

    isLoadCompleted() {
        return window.angular && angular.element(document.body).injector() && angular.element(document.body).injector().get("$http").pendingRequests == 0;
    }
}