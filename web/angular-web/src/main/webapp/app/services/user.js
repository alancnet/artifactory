import ROLES  from '../constants/roles.constants';
import EVENTS from '../constants/artifacts_events.constants';
const USER_KEY = 'USER';
const GUEST_USER = {
    name: 'anonymous',
    admin: false,
    profileUpdatable: true,
    internalPasswordDisabled: false,
    canDeploy: false,
    canManage: false,
    preventAnonAccessBuild: false,
    proWithoutLicense: false
};

class User {
    constructor(data) {
        if (data) {
            this.setData(data);
        }
    }

    setData(data) {
        if (!_.isEqual(this._data, data)) {
            data.userPreferences = data.userPreferences || {};

            angular.copy(data, this);
            this._data = data;
            User.ArtifactoryEventBus.dispatch(EVENTS.USER_CHANGED);
        }
    }

    isProWithoutLicense() {
        return this.proWithoutLicense
    }

    // Instance methods:
    isGuest() {
        return this.name === GUEST_USER.name;
    }

    isAdmin() {
        return this.admin;
    }

    isRegularUser() {
        return this.isLoggedIn() && !this.isAdmin();
    }

    isLoggedIn() {
        return !this.isGuest();
    }

    getCanManage() {
        return this.canManage || this.isProWithoutLicense();
    }

    getCanDeploy() {
        if (this.isProWithoutLicense()) {
            return false
        }
        return this.canDeploy;
    }

    canPushToBintray() {
        if (this.isProWithoutLicense()) {
            return false
        }
        return this.canDeploy;
    }

    canViewBuildState(state, stateParams, isChangeTab) {
        if (this.isProWithoutLicense()) {
            return false;
        }
        if (this.preventAnonAccessBuild && this.isGuest()) {
            return false;
        }
        if (state != 'builds.info') {
            return true;
        }
        // TODO: (Adam) this is not the place to change the tab!!!
        if (isChangeTab) {
            this.changeBuildInfoTab(stateParams);
        }

        if (stateParams.tab === 'published') {
            return true;
        }
        return this.getCanDeploy();
    }

    changeBuildInfoTab(stateParams) {
        if (this.isGuest() || !this.getCanDeploy()) {
            stateParams.tab = 'published';
        }
    }

    canView(state, stateParams = {}) {
        if (this.isProWithoutLicense()) {
            if (state === "admin.configuration.register_pro" || state === "admin.configuration" || state === "admin" ||
                    state === "home" || state === "login") {
                return true;
            } else {
                return false;
            }
        }
        if (state === "artifacts") {
            return true;
        }
        if (state.match(/^admin.security.permissions/) || state === "admin") {
            return this.getCanManage();
        }
        else if (state.match(/^admin/)) {
            return this.isAdmin();
        }
        else if (state.match(/^builds/)) {
            return this.canViewBuildState(state, stateParams, true);
        }
        else {
            return true;
        }
    }

    // Class methods:
    static login(username, remember) {
        let loginRequest = this.http.post(this.RESOURCE.AUTH_LOGIN + remember,
                angular.extend(username, {type: 'login'}));

        loginRequest.then(
                (response) => {
                    this.setUser(response.data);
                    return username;
                });
        return loginRequest;
    }

    static logout() {
        return this.http.get(this.RESOURCE.AUTH_IS_SAML, null, {}).then((res=> {
            if (res.data) {
                return this.http.get(this.RESOURCE.SAML_LOGOUT, null, {}).then((res)=> {
                    this.$window.location.replace(res.data);
                });
            }
            else {
                return this.http.post(this.RESOURCE.AUTH_LOGOUT, null, {bypassSessionInterceptor: true})
                        .then((res) => {
                            let sysMsg = this.artifactoryState.getState('systemMessage'); //we want to keep this value after logout
                            this.artifactoryState.clearAll();
                            this.artifactoryState.setState('systemMessage',sysMsg);

                            if (this.$state.current.name === 'home') {
                                this.$state.go(this.$state.current, this.$stateParams, {reload: true});
                            }

                            return this.loadUser(true);

                        }
                );
            }
        }));
    }

    static forgotPassword(user) {
        return this.http.post(this.RESOURCE.AUTH_FORGOT_PASSWORD, user);
    }

    static validateKey(key) {
        return this.http.post(this.RESOURCE.AUTH_VALIDATE_KEY + key);
    }

    static resetPassword(key, user) {
        return this.http.post(this.RESOURCE.AUTH_RESET_PASSWORD + key, user);
    }

    static canAnnotate(repoKey, path) {
        return this.http.get(this.RESOURCE.AUTH_CAN_ANNOTATE + repoKey + '&path=' + path).then((response) => {
            return response;
        });
    }

    static getLoginData() {
        return this.http.post(this.RESOURCE.AUTH_LOGIN_DATA).then((response) => {
            return response.data;//!!response.data.forgotPassword;
        });
    }

    static getOAuthLoginData() {
        return this.http.get(this.RESOURCE.OAUTH_LOGIN).then((response) => {
            return response.data;
        });
    }

    static setUser(user) {
        this.currentUser.setData(user);
        this.storage.setItem(USER_KEY, user);
        return this.currentUser;
    }

    static loadUser(force = false) {
        var user = this.storage.getItem(USER_KEY);
        if (user) {
            this.currentUser.setData(user);
        }
        if (force || !user) {
            this.whenLoadedFromServer = this.http.get(this.RESOURCE.AUTH_CURRENT, {bypassSessionInterceptor: true})
                    .then(
                    (user) => this.setUser(
                            user.data
                            //TODO need to verify with Adam
                            //user.headers && typeof(user.headers) === 'function' && user.headers().sessionvalid && user.headers().sessionvalid === 'false' ? GUEST_USER : user.data
                    )
            );
            return this.whenLoadedFromServer;
        }
        else {
            return this.$q.when(this.currentUser)
        }
    }

    static getCurrent() {
        return this.currentUser;
    }
}


export function UserFactory(ArtifactoryHttpClient, ArtifactoryStorage, RESOURCE, $q, $window, $state, $stateParams, ArtifactoryEventBus,
        ArtifactoryState) {
    // Set static members on class:
    User.http = ArtifactoryHttpClient;
    User.storage = ArtifactoryStorage;
    User.RESOURCE = RESOURCE;
    User.$q = $q;
    User.$window = $window;
    User.$state = $state;
    User.$stateParams = $stateParams;
    User.artifactoryState = ArtifactoryState;
    User.ArtifactoryEventBus = ArtifactoryEventBus;
    User.currentUser = new User();
    // Load user from localstorage:
    User.loadUser(/* force */ true);

    return User;
}
