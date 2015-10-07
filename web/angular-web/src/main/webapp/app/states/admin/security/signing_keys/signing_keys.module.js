import {SigningKeysController} from './signing_keys.controller';

function signingKeysConfig($stateProvider) {

    $stateProvider
            .state('admin.security.signing_keys', {
                url: '/signing_keys',
                templateUrl: 'states/admin/security/signing_keys/signing_keys.html',
                controller: 'SigningKeysController as SigningKeys'
            })
}

export default angular.module('security.signing_keys', [])
        .config(signingKeysConfig)
        .controller('SigningKeysController', SigningKeysController);