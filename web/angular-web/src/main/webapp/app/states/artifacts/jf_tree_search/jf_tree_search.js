import EVENTS     from '../../../constants/artifacts_events.constants';
import KEYS       from '../../../constants/keys.constants';
export function jfTreeSearch() {
    return {
        restrict: 'E',
        controller: TreeSearchController,
        controllerAs: 'TreeSearch',
        templateUrl: 'states/artifacts/jf_tree_search/jf_tree_search.html',
        bindToController: true
    }
}

class TreeSearchController {
    constructor(ArtifactoryEventBus, $element, hotkeys, $scope, $timeout) {
        this.$scope = $scope;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.$element = $element;
        this.$timeout = $timeout;
        this.term = '';
        this.showSearch = false;
        this.hotkeys = hotkeys;
        this._setupHotkeys();


        this.artifactoryEventBus.registerOnScope(this.$scope,EVENTS.ACTIVATE_TREE_SEARCH,()=>{
            $timeout(()=>{
                this._activateSearch();
            });
        });
    }

    onChange() {
        this.artifactoryEventBus.dispatch(EVENTS.TREE_SEARCH_CHANGE, this.term);
    }
    onKeydown($event) {
        // Send events to tree on key down / up / enter
        if (_.include([KEYS.ENTER, KEYS.DOWN_ARROW, KEYS.UP_ARROW, KEYS.ESC], $event.keyCode)) {
            $event.preventDefault();
            // Deactivate on enter press
            if (_.include([KEYS.ENTER, KEYS.ESC], $event.keyCode)) {
                this._deactivateSearch();
            }
            this.artifactoryEventBus.dispatch(EVENTS.TREE_SEARCH_KEYDOWN, $event.keyCode);
        }
    }
    _deactivateSearch() {
        this.term = '';
        this.showSearch = false;
        this.$searchInput().blur();
        $('body').off('click.outsideTreeSearch');
    }

    _activateSearch(key) {
        this.artifactoryEventBus.dispatch(EVENTS.TREE_SEARCH_CANCEL);
        this.showSearch = true;
        this.$timeout(()=>{
            if (!this.term) { //fix for firefox not showing the first key
                this.term = key;
                this.artifactoryEventBus.dispatch(EVENTS.TREE_SEARCH_CHANGE, this.term);
            }
        });
        this.$searchInput().focus();
        $('body').on('click.outsideTreeSearch', (e) => {
            if (!$(this.$element).has(e.target).length) {
                this.artifactoryEventBus.dispatch(EVENTS.TREE_SEARCH_CANCEL);
                this._deactivateSearch();
                if (!this.$scope.$$phase) this.$scope.$digest();
            }
        });

    }

    $searchInput() {
        return this.$element.find('input')[0];
    }

    _setupHotkeys() {
        this.hotkeys.bindTo(this.$scope).add({
            combo: KEYS.HOTKEYS.ALPHANUMERIC.split(''),
            description: 'Any alphanumeric key to search the tree',
            callback: (event, hotkey) => {
                if (_.contains(event.target.classList, 'jstree-anchor')) {
//                    var key = hotkey.format()[0];
                    var key = String.fromCharCode(event.which);
                    this._activateSearch(key);
                }
            }
        });

    }

}