export function jfValidatorCron(CronTimeDao, $q, $timeout, ArtifactoryNotifications) {
    return {
        restrict: 'A',
        require: 'ngModel',
        /**
         * Register an async validator on cron expressions
         */
        link: function jfCronValidatorLink(scope, element, attrs, ngModel) {
            let cronTimeDao = CronTimeDao.getInstance();

            let cache = {};

            // Get from server and cache result, or get from cache
            function getFromServer(data) {
                if (!cache[data.cron]) {
                    cache[data.cron] = cronTimeDao.get(data).$promise;
                }
                return cache[data.cron];
            }

            // This is a factory function that creates a validation function for the cron expression
            // It will be executed per validation key, and return an error only if the server returns
            // an error that matches the key
            function validateCron(key) {
                return function (modelValue, viewValue) {
                    var value = modelValue || viewValue;

                    if (!value) {

                        return $q.when();
                    }
                    // No need to check with server if the cron is less than 11 chars
                    if (value.length < 11) {
                        if (key === 'invalidCron') {
                            return $q.reject();
                        }
                        else {
                            return $q.when();
                        }
                    }

                    let data = {cron: value};
                    if (attrs.jfValidatorCronIsReplication) {
                        data.isReplication = true;
                    }

                    // Get from server (or cached result)
                    return getFromServer(data)
                            .catch(function (result) {
                                if (result.data.error === key || (result.data.feedbackMsg && result.data.feedbackMsg.error === key)) {
                                    // The server responded with error message that matches this validator
                                    if (key === 'shortCron') {
                                        // shortCron is not an invalid value, just notify the user about it:
                                        ArtifactoryNotifications.create({warn: "The current Cron expression will " +
                                        "result in very frequent replications. \nThis will impact system performance."});
                                        return true;
                                    }
                                    return $q.reject();
                                }
                                return true;
                            });
                }
            }

            // Message is per key, so need 3 different keys (even though REST is the same)
            // Possible keys: invalid cron expression, next execution is too close in the future, next execution is in the past
            ngModel.$asyncValidators.invalidCron = validateCron('invalidCron');
            ngModel.$asyncValidators.shortCron = validateCron('shortCron');
            ngModel.$asyncValidators.pastCron = validateCron('pastCron');
        }
    }
}