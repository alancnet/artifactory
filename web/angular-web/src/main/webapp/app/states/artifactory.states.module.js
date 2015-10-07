import AdminState       from './admin/admin.module';
import ArtifactState    from './artifacts/artifacts.module';
import BuildsState      from './builds/builds.module';
import HomeModule       from './home/home.module';
import Login            from './login/login.module';
import OAuthRequest     from './oauth/login_request/login_request.module';
import ForgotPassword   from './forgot_password/forgot_password.module';
import UserProfile      from './user_profile/user_profile.module';
import ServerError      from './server_error/server_error.module';
import BaseState        from './base/base.module';
import EVENTS from '../constants/artifacts_events.constants';

angular.module('artifactory.states', [
    AdminState.name,
    ArtifactState.name,
    BuildsState.name,
    HomeModule.name,
    Login.name,
    OAuthRequest.name,
    ForgotPassword.name,
    UserProfile.name,
    ServerError.name,
    BaseState.name,
    'artifactory.services',
    'artifactory.dao',
    'cfp.hotkeys',
    'ui.router'
]).
run(preventAccessToPagesByPermission);
function preventAccessToPagesByPermission(User, $rootScope, ArtifactoryNotifications, $location, $timeout,
        ArtifactoryFeatures, FooterDao, ArtifactoryState, ArtifactoryEventBus) {
    $rootScope.$on('$stateChangeStart', (e, toState, toParams, fromState, fromParams) => {
        // Permissions:

        if (fromState.name && toState.name && fromState.name != toState.name) {
            ArtifactoryEventBus.dispatch(EVENTS.CANCEL_SPINNER);
        }

        if (toState.name === 'login' && $location.path() !== '/login' && $location.path() !== '/oauth2/loginRequest') {
            let afterLogin = ArtifactoryState.getState('urlAfterLogin');
            if (!afterLogin) ArtifactoryState.setState('urlAfterLogin', $location.path());
        }

        if (!User.getCurrent().canView(toState.name, toParams)) {
            if (User.getCurrent().isProWithoutLicense()) {
                $timeout(() => $location.path('admin/configuration/register_pro'));
            }else {
                if ($location.path() !== '/login') ArtifactoryState.setState('urlAfterLogin', $location.path());
                ArtifactoryNotifications.create({error: 'You are not authorized to view this page'});
                e.preventDefault();
                $timeout(() => $location.path('/login'));
            }
        }
        // Features per license:
        else {
            let feature = toParams.feature;
            // Must verify footer data is available before checking (for initial page load)
            FooterDao.get().then(() => {
                if (ArtifactoryFeatures.isDisabled(feature) || ArtifactoryFeatures.isHidden(feature)) {
                    ArtifactoryNotifications.create({error: 'Page unavailable'});
                    e.preventDefault();
                    $timeout(() => $location.path('/home'));
                }
            });
        }

        if (!e.defaultPrevented) {
            ArtifactoryEventBus.dispatch(EVENTS.CLOSE_MODAL);
        }
    })
}
