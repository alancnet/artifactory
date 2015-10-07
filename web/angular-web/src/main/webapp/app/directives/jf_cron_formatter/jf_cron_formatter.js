export function jfCronFormatter(CronTimeDao, $q, $timeout) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function jfCronFormatterLink(scope, element, attrs, ngModel) {


            let cronTimeDao = CronTimeDao.getInstance();
            ngModel.$formatters.push(formatCron);
            ngModel.$parsers.push(input => ngModel.$modelValue);

            // Format the next scheduled time in the server
            function formatCron(input) {
                if (input) {
                    return cronTimeDao.get({cron: input}).$promise
                        .then(function (result) {
                            if (result.error) {
                                return $q.reject();
                            }

                            ngModel.$viewValue = result.nextTime;
                            ngModel.$render();
                            return ngModel.$viewValue;
                        });
                }

                return input;
            }
        }
    }
}