import EVENTS from '../../constants/artifacts_events.constants';
import API from '../../constants/api.constants';
import HELP from '../../constants/artifactory_help.constants';

class jfHeaderController {
    constructor($scope, $q, User, $state, $timeout, $window, GeneralConfigDao, FooterDao, ArtifactoryEventBus, ArtifactoryNotifications, $rootScope, $location, $http, ArtifactoryState) {
        this.$scope = $scope;
        this.currentUser = User.getCurrent();
        this.generalConfigDao = GeneralConfigDao;
        this.footerDao = FooterDao;
        this.artifactoryState = ArtifactoryState;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.user = User;
        this.state = $state;
        this.$timeout = $timeout;
        this.$window = $window;
        this.$q = $q;
        this.logoEndPoint = `${API.API_URL}/auth/screen/logo`;
        this.defaultLogoUrl = 'images/artifactory_logo.svg';
        this.HELP = HELP;

        //$.getJSON('artifactory_help.json', (jsonRes) => {
        //    this.HELP = jsonRes;
        //    this._refreshHelpMenu($location.path());
        //})
        //        .fail((errRes) => {
        //            if (errRes.status != 404) {
        //                let body = `Cannot parse the local help links file 'artifactory_help.json'.<br>The default file will be loaded instead.`;
        //                ArtifactoryNotifications.createMessageWithHtml({type: 'error', body: body, timeout: 0});
        //            }
        //        });

        $rootScope.$watch(() => {
            return $location.path();
        },
        (currentURL) => this._refreshHelpMenu(currentURL));

        this._registerEvents();
        this._getLogoUrlAndServerName();
    }

    _registerEvents() {
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.LOGO_UPDATED, () => this._getLogoUrlAndServerName());
    }

    _getLogoUrlAndServerName() {
        this.footerDao.get(true).then(footerData => {
            this.$window.document.title = footerData.serverName ? footerData.serverName : 'Artifactory';

            this.helpLinksEnabled = footerData.helpLinksEnabled;

            if (footerData.userLogo) {
                this.logoUrl = '';
                this.$timeout(()=> {
                    this.logoUrl = this.logoEndPoint;
                });
            }
            else if (footerData.logoUrl)
                this.logoUrl = footerData.logoUrl;
            else
                this.logoUrl = this.defaultLogoUrl;

            if ((this.user.currentUser.name !== 'anonymous' || this.user.currentUser.anonAccessEnabled) && footerData.systemMessage)
                this.artifactoryState.setState('systemMessage', {
                    enabled: footerData.systemMessageEnabled,
                    color: footerData.systemMessageTitleColor,
                    title: footerData.systemMessageTitle,
                    message: footerData.systemMessage,
                    inAllPages: footerData.showSystemMessageOnAllPages
                });
            else
                this.artifactoryState.setState('systemMessage',undefined);
        });
    }

    _refreshHelpMenu(currentURL) {
        this.helpLinks = [];

        for (let key in this.HELP)
            if (currentURL == key || (key.indexOf('**') != -1 && currentURL.indexOf(key.replace('**', '')) != -1))
                for (let i = 0; i < this.HELP[key].length; i++)
                    this.helpLinks.push(this.HELP[key][i]);
    }

    logout() {
        this.user.logout()
                .then(() => {
                    this.state.go("home");
                });
    }
}

export function jfHeader() {
    return {
        scope: {
            hideSearch: '@'
        },
        controller: jfHeaderController,
        controllerAs: 'jfHeader',
        bindToController: true,
        templateUrl: 'directives/jf_header/jf_header.html'
    }
}