export function jfValidatorPathPattern() {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function jfValidatorPathPattern(scope, element, attrs, ngModel) {

            registerTransformers();

            function registerTransformers() {
                ngModel.$validators.pathPattern = validatePathPattern;
            }

            function validatePathPattern(modelValue, viewValue) {
                if (!viewValue) return false;

                let ok = true;

                let tokens = ["(org|orgPath)", "module", "baseRev"];

                for (let i in tokens) {
                    let token = tokens[i];
                    let regex = new RegExp('\\['+token+'\\]');
                    if (!viewValue.match(regex)) {
                        ok = false;
                        break;
                    }
                }

                return ok;
            }
        }
    }
}