import {ReplaceCharacter} from './replace_character';
import {FileSize} from './filesize';
import {SplitWords} from './split_words';
import {ParseLinks} from './parse_links';
import {ReplaceStringForAol} from './replace_string_for_aol';

export default angular.module('artifactory.filters', [])
        .filter('replaceCharacter', ReplaceCharacter)
        .filter('filesize', FileSize)
        .filter('splitWords', SplitWords)
        .filter('parseLinks', ParseLinks)
        .filter('replaceStringForAol', ReplaceStringForAol);