/**
 * Validates an input to be valid entiyy name
 */
export function jfValidatorName(NameValidatorDao, $q, $timeout) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function jfValidatorName(scope, element, attrs, ngModel) {

            let nameValidatorDao = NameValidatorDao.getInstance();
            ngModel.$asyncValidators.name = validateName;

            function validateName(modelValue, viewValue) {
                var value = modelValue || viewValue;

                if (!value) {
                    return $q.when();
                }

                return nameValidatorDao.get({name: value}).$promise
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