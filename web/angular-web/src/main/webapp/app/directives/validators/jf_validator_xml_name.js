/**
 * Validates an input to be valid XMLCName
 */
export function jfValidatorXmlName(XmlNameDao, $q, $timeout) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function jfValidatorXmlName(scope, element, attrs, ngModel) {

            let xmlNameDao = XmlNameDao.getInstance();
            ngModel.$asyncValidators.xmlName = validateXmlName;

            function validateXmlName(modelValue, viewValue) {
                var value = modelValue || viewValue;

                if (!value) {
                    return $q.when();
                }

                return xmlNameDao.get({xmlName: value}).$promise
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