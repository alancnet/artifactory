export function jfValidatorIntValue() {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function jfIntegerValueValidator(scope, element, attrs, ngModel) {

            registerTransformers();

            function registerTransformers() {
                ngModel.$validators.integerValue = validateInteger;
            }

            function validateInteger(modelValue, viewValue) {
                return parseInt(viewValue).toString() === viewValue;
            }
        }
    }
}