export function jfSpecialChars() {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function jfSpecialCharsLink(scope, element, attrs, ngModel) {

            let disallowedSymbols = [ '/', '\\', '<', '>', '~', '!', '@', '#', '$',
                                    '%', '^', '&', '(', ')', '+', '=', '-', '{', '}',
                                    '[', ']', ';', ',', '`', ',', ' '];

            ngModel.$validators.validCharacters = function(modelValue, viewValue) {
                let value = modelValue || viewValue;
                if (!value) {
                    return true;
                }
                value = value.split('');
                let valid = true;

                for (let i = 0, limit = value.length; i < limit; i++) {
                    if (disallowedSymbols.indexOf(value[i]) > -1) {
                                valid = false;
                    }
                }
                return valid;
            }
        }
    }
}