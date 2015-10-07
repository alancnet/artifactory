import {mlPushMenu} from './mlpushmenu';
import EVENTS from '../../constants/artifacts_events.constants';

class jfSidebarController {

    constructor(FooterDao, AdminMenuItems, $state, User, $timeout, $rootScope, ArtifactoryEventBus, $scope, ArtifactoryFeatures, ArtifactoryStorage) {
        this.currentTab = "Home";
        this.$state = $state;
        this.$timeout = $timeout;
        this.setSideBarState();
        this.originalAdminMenuItems = AdminMenuItems;
        this.adminMenuItems = [];
        this.user = User.getCurrent();
        this.ArtifactoryEventBus = ArtifactoryEventBus;
        this.features = ArtifactoryFeatures;
        this.storage = ArtifactoryStorage;
        this.footerDao = FooterDao;

        User.whenLoadedFromServer.then(this._onUserLoaded.bind(this));
        ArtifactoryEventBus.registerOnScope($scope, EVENTS.USER_CHANGED, this._onUserLoaded.bind(this));
        ArtifactoryEventBus.registerOnScope($scope, EVENTS.FOOTER_REFRESH, ()=> {
            this.getFooterData()
        });

        $rootScope.$on('$stateChangeSuccess', () => this.setSideBarState());
        this.getFooterData();
    }

    getFooterData(FooterDao) {
        this.footerDao.get(true).then(footerData => this.footerData = footerData);
    }

    setSideBarState() {
        this.isAdminOpen = _.includes(this.$state.current.name, 'admin');
    }

    isCurrentTab(tab) {
        return this.currentTab === tab.label;
    }

    goBack(state) {
        let prevState = this.storage.getItem('stateBeforeAdmin');
        if (prevState) {
            this.$state.go(prevState.name, prevState.params);
        }
        else {
            this.$state.go('home');
        }

        document.getElementById('sidebar-wrapper').scrollTop = 0;
    }

    setCurrentTab(tab) {
        this.currentTab === tab.label ? this.currentTab = '' : this.currentTab = tab.label;
    }

    _onUserLoaded(AdminMenuItems) {
        this.menuItems = this._getMenuItems();
        this._fixAdminMenuItems();
        this.$timeout(() => {
            new mlPushMenu(document.getElementById('mp-menu'), {
                type: 'cover'
            });
        });
    }

    goToState(item) {
        if (item.state === 'artifacts.browsers.path') {
            this.ArtifactoryEventBus.dispatch(EVENTS.CLEAR_SEARCH);
        }
        // Fix browser param according to user preference
        if (item.stateParams && item.stateParams.browser) {
            let storedBrowser = this.storage.getItem('BROWSER');
            item.stateParams.browser = storedBrowser || 'tree';
            item.stateParams.tab = storedBrowser === 'stash' ? 'StashInfo' : 'General';
        }

        // If we're going into admin - save the state
        if (item.state == 'admin' && this.$state.current.name.indexOf('admin') === -1) {
            this.storage.setItem('stateBeforeAdmin', {name: this.$state.current.name, params: this.$state.params});
        }

        this.$state.go(item.state, item.stateParams);
    }

    _getMenuItems() {
        return [
            {
                label: 'Home',
                stateParent: "home",
                state: "home",
                icon: 'icon icon-home',
                selected: true
            },
            {
                label: 'Artifacts',
                state: "artifacts.browsers.path",
                stateParent: "artifacts",
                stateParams: {tab: 'General', artifact: '', browser: 'tree'},
                icon: 'icon icon-artifacts',
                isDisabled: !this.user.canView('artifacts'),
                selected: false
            },
            {
                label: 'Builds',
                stateParent: "builds",
                state: "builds.all",
                icon: 'icon icon-builds',
                selected: false,
                isDisabled: !this.user.canView("builds")
            },
            {
                label: 'Admin',
                icon: 'icon icon-admin',
                id: 'admin',
                stateParent: "admin",
                state: 'admin',
                selected: false,
                children: true,
                isDisabled: !this.user.getCanManage()
            }
        ]
    }
    _fixAdminMenuItems() {
        angular.copy(this.originalAdminMenuItems, this.adminMenuItems);
        this.adminMenuItems.forEach((item) => {
            item.isDisabled = true;
             // if all subitems are hidden then hide item
            item.isHidden = _.every(item.subItems, (subitem) => this.features.isHidden(subitem.feature));
            item.subItems.forEach((subitem) => {
                if (!this.user.canView(subitem.state) ||
                    this.features.isDisabled(subitem.feature))
                {
                    subitem.isDisabled = true;
                }
                else { // if one subitem is enabled then item is enabled
                    item.isDisabled = false;
                }
            });
        });
    }
}

export function jfSidebar() {
    return {
        controller: jfSidebarController,
        controllerAs: 'jfSidebar',
        templateUrl: 'directives/jf_sidebar/jf_sidebar.html'
    }
}