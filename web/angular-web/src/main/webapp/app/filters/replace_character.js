export function ReplaceCharacter() {

    return function(input, search, replace) {
        if (input) {
            let regex = new RegExp(search, 'g');
            return input.replace(regex, replace);
        }

        return input;
    }
}