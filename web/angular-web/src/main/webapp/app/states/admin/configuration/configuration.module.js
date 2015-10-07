import Licenses           from './licenses/licenses.module';
import Mail               from './mail/mail.module';
import Proxies            from './proxies/proxies.module';
import RegisterPro        from './register_pro/register_pro.module';
import Bintray            from './bintray/bintray.module';
import BlackDuck          from './black_duck/black_duck.module'
import General            from './general/general.module';
import PropertySets       from './property_sets/property_sets.module';
import HighAvailability   from './ha/ha.module';


import {AdminConfigurationController} from './configuration.controller';

/**
 * configuration and state definition
 * @param $stateProvider
 */
function configurationConfig($stateProvider) {

    $stateProvider
            .state('admin.configuration', {
                url: '/configuration',
                template: '<ui-view></ui-view>',
                controller: 'AdminConfigurationController as AdminConfiguration'
            })
}

/**
 * Module definition
 */
export default angular.module('admin.configuration', [
    Licenses.name,
    Mail.name,
    Proxies.name,
    RegisterPro.name,
    Bintray.name,
    BlackDuck.name,
    General.name,
    PropertySets.name,
    HighAvailability.name

])
        .config(configurationConfig)
        .controller('AdminConfigurationController', AdminConfigurationController);