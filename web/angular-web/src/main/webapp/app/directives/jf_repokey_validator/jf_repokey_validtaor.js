export function jfRepokeyValidator(RepositoriesDao, $q, $timeout) {
    return {
        restrict: 'A',
        require: 'ngModel',
        scope:{
            controller:'=jfRepokeyValidator'
        },
        link: function jfRepokeyValidatorLink(scope, element, attrs, ngModel) {


            ngModel.$asyncValidators.repoKeyValidator = validateRepoKey;

            function validateRepoKey(modelValue, viewValue) {
                var value = modelValue || viewValue;

                if (!value) {
                    return $q.when();
                }

                return RepositoriesDao.repoKeyValidator({repoKey: value}).$promise
                    .then(function (result) {
                        if (result.error) {
                            scope.controller.repoKeyValidatorMessage = result.error;
                            return $q.reject();
                        }
                        return true;
                    });
            }
        }
    }
}