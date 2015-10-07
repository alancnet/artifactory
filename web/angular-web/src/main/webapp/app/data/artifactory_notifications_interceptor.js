/**
 * returns a function that accept some custom info
 * and returns the interceptor object.
 * intent to be injected and use in DAO's
 *
 * @returns {Function}
 */
export function artifactoryNotificationsInterceptor($q, ArtifactoryNotifications) {

    /**
     * accept an additional info that can be used
     * in the returned interceptor object
     *
     * @returns {{response: Function, responseError: Function}}
     */
    return {
        response: function (res) {
            if (res.data) {
                if (!res.data.url) {
                    if (res.data.info || res.data.warn) {
                        ArtifactoryNotifications.create(res.data);
                    } else if (res.data.feedbackMsg) {
                        ArtifactoryNotifications.create(res.data.feedbackMsg);
                    }
                }
            }
            return res;
        },
        responseError: function (res) {
            // Response error as array:
            if (res.data && res.data.errors && res.data.errors.length) {
                ArtifactoryNotifications.create({error: res.data.errors[0].message});
            }
            // Response error as single object:
            else if (res.data && (res.data.error || res.data.warn)) {
                ArtifactoryNotifications.create(res.data);
            }
            return $q.reject(res);
        }
    }
}