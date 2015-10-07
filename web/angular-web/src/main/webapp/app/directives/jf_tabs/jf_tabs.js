import EVENTS from '../../constants/artifacts_events.constants';

class jfTabsController {

    constructor($scope, $state, $timeout, $element, $stateParams, ArtifactoryEventBus, ArtifactoryFeatures) {
        this.$scope = $scope;
        this.$element = $element;
        this.stateParams = $stateParams;
        this.$timeout = $timeout;
        this.state = $state;
        this.tabsCollapsed = [];
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.features = ArtifactoryFeatures;

        this.currentTab = {
            name: this.stateParams.tab
        };
        this._registerEvents();
        this.initTabs();
    }

    initTabs() {
        // wait for the element to render and calculate how many tabs should display
        this.$timeout(() => {
            this._calculateTabsSize();

            if (!this._getTab(this.currentTab)) {
                // If current tab doesn't exist on the tabs list at all - select the first tab
                this.onClickTab(this.tabs[0]);
            }
            else {
                // Otherwise - make sure it's visible
                this._ensureTabVisible(this.currentTab);
            }
        });
    }

    _calculateTabsSize() {
        // wait for the element to render and calculate how many tabs should display
        let container = $(this.$element).children().eq(0);
        let containerWidth = container.width();
        let tabWidth = parseInt(this.tabWidth);
        let containerMargin = parseInt(this.containerMargin);

        let expanderWidth = $('.action-expand').eq(0).outerWidth(true);
        let tabsToTake = Math.floor((containerWidth - expanderWidth - containerMargin) / tabWidth);

        this.tabsCollapsed = _.takeRight(this.tabs, this.tabs.length - tabsToTake);
        this.tabsVisible = _.take(this.tabs, tabsToTake);
    }

    _registerEvents() {
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TABS_REFRESH, () => this.initTabs());
        // URL changed (like back button / forward button / someone input a URL)
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.ARTIFACT_URL_CHANGED, (stateParams) => {
            this.currentTab = {name: stateParams.tab};
        });

        $(window).on('resize.tabs', () => {
            this.initTabs();
            this.$scope.$digest();
        });
        this.$scope.$on('$destroy', () => {
            $(window).off('resize.tabs');
        });
    }

    onClickTab(tab) {
        // if the tab is in the more section replace it
        // with the last tab in the main tabs.
        if (this.features.isDisabled(tab.feature) || tab.isDisabled) {
            return;
        }
        this._ensureTabVisible(tab);
        this.state.go(this.state.current, {tab: tab.name});
        this.currentTab.name = tab.name;
    }

    _ensureTabVisible(tab) {
        let collapsedTab = this._getCollapsedTab(tab);
        if (!collapsedTab) return;

        // Replace between collapsedTabs & visibleTabs:
        var collapsedTabIndex = this.tabsCollapsed.indexOf(collapsedTab)
        var tabToReplace = this.tabsVisible[this.tabsVisible.length - 1]
        this.tabsCollapsed[collapsedTabIndex] = tabToReplace;
        this.tabsVisible[this.tabsVisible.length - 1] = collapsedTab;
    }

    isActiveTab(tab) {
        return tab.name === this.currentTab.name;
    }

    _getTab(tab) {
        return _.findWhere(this.tabs, {name: tab.name});
    }

    _getCollapsedTab(tab) {
        return _.findWhere(this.tabsCollapsed, {name: tab.name})
    }
}

export function jfTabs() {
    return {
        scope: {
            tabs: '=',
            dictionary: '=',
            features: '=',
            tabWidth: '@',
            containerMargin: '@'
        },
        transclude: true,
        controller: jfTabsController,
        controllerAs: 'jfTabs',
        templateUrl: 'directives/jf_tabs/jf_tabs.html',
        bindToController: true
    }
}