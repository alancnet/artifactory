window._sessionExpire = function () {
    localStorage._forceSessionExpire = true;
}

export function artifactorySessionInterceptor($injector) {
    var User;
    var $state;
    var ArtifactoryState;
    var $location;
    var RESOURCE;
    var $q;
    var ArtifactoryNotifications;
    var ArtifactoryHttpClient;
    var $window;

    function initInjectables() {
        $q = $q || $injector.get('$q');
        $window = $window || $injector.get('$window');
        User = User || $injector.get('User');
        $state = $state || $injector.get('$state');
        ArtifactoryState = ArtifactoryState || $injector.get('ArtifactoryState');
        $location = $location || $injector.get('$location');
        RESOURCE = RESOURCE || $injector.get('RESOURCE');
        ArtifactoryNotifications = ArtifactoryNotifications || $injector.get('ArtifactoryNotifications');
        ArtifactoryHttpClient = ArtifactoryHttpClient || $injector.get('ArtifactoryHttpClient');
    }

    function bypass(res) {
        return res.config && res.config.bypassSessionInterceptor;
    };

    function isSessionInvalid(res) {
        return res.headers().sessionvalid === "false";
    }

    function isApiRequest(res) {
        return _.contains(res.config.url, RESOURCE.API_URL);
    }

    function isLoggedIn() {
        return !User.getCurrent().isGuest();
    }

    function handleExpiredSession() {
        // if session invalid and we think we are logged in - session expired on server
        delete localStorage._forceSessionExpire;
        User.loadUser(true);


        if ($state.current !== 'login' && $location.path() !== '/login') {
            ArtifactoryState.setState('urlAfterLogin', $location.path());
            $state.go('login');
//            return false;
        }
        return true;
    }

    function verifySession(res) {
        initInjectables();
        if (bypass(res)) {
            return true;
        }

        User.loadUser(); // Refresh from localstorage (parallel tab support)
        if (isApiRequest(res) && isSessionInvalid(res) && isLoggedIn() || localStorage._forceSessionExpire) {
            // if the user is not logged in but is in a bypassed request
            // let the request go through but log out the user.
            if ($location.path() !== '/login') ArtifactoryState.setState('urlAfterLogin', $location.path());
            return handleExpiredSession();
        }
        return true;
    }

    function checkAuthorization(res) {
        if (res.status === 401) {
            ArtifactoryHttpClient.post("/auth/loginRelatedData", null,{}).then((res)=>{
               if(res.data.ssoProviderLink) {
                   if ($location.path() == '/login') {
                       $state.go('login');
                   } else {
                        $window.open(res.data.ssoProviderLink, "_self");
                   }
               } else {
                   setUrlAfterLogin();
                   $state.go('login');
               }
            });
        }
    }

    function setUrlAfterLogin() {
        if ($state.current !== 'login' && $location.path() !== '/login') {
            ArtifactoryState.setState('urlAfterLogin', $location.path());
        }
    }

    function response(res) {
        if (verifySession(res)) {
            return res;
        }
        else {
            return $q.reject(res);
        }
    }

    function responseError(res) {
        verifySession(res);
        checkAuthorization(res);
        return $q.reject(res);
    }

    return {
        response: response,
        responseError: responseError
    };
}
