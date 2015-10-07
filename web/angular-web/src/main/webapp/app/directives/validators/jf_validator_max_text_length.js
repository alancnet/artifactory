export function jfValidatorMaxTextLength() {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function jfLimitTextLengthValidator(scope, element, attrs, ngModel) {

            let limitTo = attrs.maxlength || attrs.jfValidatorMaxTextLength;

            registerTransformers();

            function registerTransformers() {
                ngModel.$validators.maxlength = validateTextLength;
            }

            function validateTextLength(modelValue, viewValue) {
                let ok  = !viewValue || viewValue.length <= parseInt(limitTo);
                return ok;
            }
        }
    }
}