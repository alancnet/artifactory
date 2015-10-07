/**
 * Created by gidis on 7/26/15.
 */
import EVENTS from '../constants/artifacts_events.constants';

export function artifactoryMessageInterceptor(ArtifactoryState, $q) {

    function request(req) {
        return req;
    }

    function response(res) {
        handleResponse(res);
        return res;
    }

    function responseError(res) {
        handleResponse(res);
        return $q.reject(res);
    }

    function handleResponse(res) {
        let messages=res.headers()["artifactory-ui-messages"];
        if (messages) ArtifactoryState.setState('constantMessages', JSON.parse(messages));
    }

    return {
        response: response,
        request: request,
        responseError: responseError
    };
}
