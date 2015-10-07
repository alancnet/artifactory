export function jfValidatorDateFormat(DateFormatDao, $q, $timeout) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function jfValidatorDateFormat(scope, element, attrs, ngModel) {

            let dateFormatDao = DateFormatDao.getInstance();
            ngModel.$asyncValidators.dateFormatExpression = validateDateFormat;

            function validateDateFormat(modelValue, viewValue) {
                var value = modelValue || viewValue;

                if (!value) {
                    return $q.when();
                }

                return dateFormatDao.get({dateformat: value}).$promise
                    .then(function (result) {
                        if (result.error) {
                            return $q.reject();
                        }
                        return true;
                    });
            }
        }
    }
}