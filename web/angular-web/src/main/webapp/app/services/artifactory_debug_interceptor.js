/**
 * returns a function that accept some custom info
 * and returns the interceptor object.
 * intent to be injected and use in DAO's
 *
 * @returns {Function}
 */
window._debugOn = function() {
    localStorage._debug = true;
}
window._debugOff = function() {
    delete localStorage._debug;
}
export function artifactoryDebugInterceptor($injector) {
    /**
     * accept an additional info that can be used
     * in the returned interceptor object
     *
     * @returns {{response: Function, responseError: Function}}
     */
    var $q;
    var RESOURCE;
    function debugResponse(res) {
        if (!localStorage._debug) return;
        RESOURCE = RESOURCE || $injector.get('RESOURCE');
        var apiRequest = _.contains(res.config.url, RESOURCE.API_URL);

        if (apiRequest) {
            console.log("========================");
            console.log("URL:      ",res.config.url);
            console.log("METHOD:   ",res.config.method);
            console.log("DATA:     ",res.config.data);
            console.log("Status:   ",res.status);
            console.log("Response: ", res.data);
            console.log("========================");
        }
    }
    function response(res) {
        debugResponse(res);
        return res;
    }
    function responseError(res) {
        $q = $q || $injector.get('$q');
        debugResponse(res);
        return $q.reject(res);
    }
    return {
        response: response,
        responseError: responseError
    };
}
