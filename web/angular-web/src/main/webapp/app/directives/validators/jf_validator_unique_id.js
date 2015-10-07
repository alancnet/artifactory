/**
 * Validates an input to be unique id in the config descriptor
 */
export function jfValidatorUniqueId(UniqueIdValidatorDao, $q, $timeout) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function jfValidatorUniqueId(scope, element, attrs, ngModel) {

            let uniqueIdDao = UniqueIdValidatorDao.getInstance();
            ngModel.$asyncValidators.uniqueId = validateUniqueId;

            function validateUniqueId(modelValue, viewValue) {
                // Don't validate disabled fields
                if ($(element).is(':disabled')) return $q.when();

                var value = modelValue || viewValue;

                if (!value) return $q.when();

                return uniqueIdDao.get({id: value}).$promise
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