import EVENTS     from '../../constants/common_events.constants';

export class LoginController {

    constructor($state, User, $location, $window, ArtifactoryState, ArtifactoryEventBus) {
        this.user = {};
        this.rememberMe = false;
        this.UserService = User;
        this.$state = $state;
        this.$window = $window;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.$location = $location;
        this.ArtifactoryState = ArtifactoryState;
        this.canResetPassword = false;
        this.loginForm = null;

        this.canExit = (User.currentUser.name !== 'anonymous' || User.currentUser.anonAccessEnabled);

        this.checkResetPassword();
    }

    login() {

        this.artifactoryEventBus.dispatch(EVENTS.FORM_SUBMITTED);

        if (this.loginForm.$valid) {
            this.UserService.login(this.user, this.rememberMe).then(success.bind(this), error.bind(this))
        }

        function success(result) {
            this.user = result.data;
            let urlAfterLogin = this.ArtifactoryState.getState('urlAfterLogin');
            if (urlAfterLogin) {
                this.$location.path(urlAfterLogin)
            }
            else {
                this.$state.go('home');
            }
        }

        function error(response) {
            if (response.data) {
                this.errorMessage = response.data.error;
            }
        }
    }

    userPasswordChanged() {
        this.errorMessage = null;
    }


    checkResetPassword() {
        this.UserService.getLoginData().then((response) => {
            this.canResetPassword = response.forgotPassword;
            this.ssoProviderLink = response.ssoProviderLink;
            this.oauthProviderLink = response.oauthProviderLink;
        });
    }

    gotoForgotPwd() {
        this.$state.go('forgot-password');
    }

    oauthLogin() {
        this.$window.open(this.oauthProviderLink,'_self');
    }

    ssoLogin() {
        this.$window.open(this.ssoProviderLink,'_self');
    }
}
