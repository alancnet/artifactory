class jfHeaderSearchController {
    constructor($element, $scope,$state) {
        this.$scope = $scope;
        this.$element = $element;
        this.$searchTrigger = $('[data-ic-class="search-trigger"]');
        this.$searchInput = $('[data-ic-class="search-input"]');

        this.$state=$state;
        this._isFocused = false;
        this.query = {
            search:'quick'
        };
        this._registerEvents();
    }

    search() {
        this.$state.go('artifacts.browsers.search',{'searchType':'quick', 'tab':this.query.search,'params':btoa(JSON.stringify(this.query))});
        this.query.query = '';
    }

    _registerEvents() {
        this.$element.on('mouseenter', () => this._mouseEnter());
        this.$element.on('mouseleave', () => this._mouseLeave());
        this.$searchInput.on('focus', () => this._triggerFocus());
        this.$searchInput.on('blur', () => this._triggerFocus());

        this.$scope.$on('$destroy', () => {
            this.$element.off('mouseenter');
            this.$element.off('mouseleave');
            this.$searchInput.off('focus');
            this.$searchInput.off('blur');
        });
    }

    _registerBodyEvent() {
        $('body').on('click.outsideSearch', (e) => this._checkClickTarget(e));
    }

    _deregisterBodyEvent() {
        $('body').off('click.outsideSearch');
    }

    _mouseEnter() {
        this.$searchTrigger.addClass('active');
        this.$searchInput.addClass('active');
    }

    _mouseLeave() {
        // do not hide the search bar when it is focused
        if (this._isFocused) {
            return;
        }

        this.$searchTrigger.removeClass('active');
        this.$searchInput.removeClass('active');
    }

    _triggerFocus() {
        this._registerBodyEvent();
        this._isFocused = !this._isFocused;
    }

    _checkClickTarget(e) {
        // check if the clicked element is inside the directive
        if (!$(this.$element).has(e.target).length) {
            this.query.query = '';
            if (!this.$scope.$$phase) this.$scope.$digest();
            this._isFocused = false;
            this._mouseLeave();
            this._deregisterBodyEvent();
        }
    }


}

export function jfHeaderSearch() {
    return {
        controller: jfHeaderSearchController,
        controllerAs: 'jfHeaderSearch',
        templateUrl: 'directives/jf_header_search/jf_header_search.html'
    }
}