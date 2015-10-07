export function artifactoryServerErrorInterceptor($injector) {
    var $state;
    var $q;

    function initInjectables() {
        $q = $q || $injector.get('$q');
        $state = $state || $injector.get('$state');
    }

    function responseError(res) {
        initInjectables();
        if (res.status === 0 || res.status > 500) {
            $state.go('server_error');
        }
        return $q.reject(res);
    }

    return {
        responseError: responseError
    };
}

