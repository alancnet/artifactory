import {AdminConfigurationMailController} from './mail.controller';

function mailConfig($stateProvider) {
    $stateProvider
            .state('admin.configuration.mail', {
                params: {feature: 'mail'},
                url: '/mail',
                templateUrl: 'states/admin/configuration/mail/mail.html',
                controller: 'AdminConfigurationMailController as AdminConfigurationMail'
            })
}

export default angular.module('configuration.mail', [])
        .config(mailConfig)
        .controller('AdminConfigurationMailController', AdminConfigurationMailController)