import {AdminConfigurationPropertySetsController}    from './property_sets.controller';
import {AdminConfigurationPropertySetFormController} from './property_set_form.controller';
import {PropertyFormModalFactory}                    from './property_form_modal';
function propertySetsConfig($stateProvider) {
    $stateProvider
            .state('admin.configuration.property_sets', {
                url: '/property_sets',
                controller: 'AdminConfigurationPropertySetsController as PropertySets',
                templateUrl: 'states/admin/configuration/property_sets/property_sets.html'
            })
            .state('admin.configuration.property_sets.edit', {
                parent: 'admin.configuration',
                url: '/property_sets/{propertySetName}/edit',
                templateUrl: 'states/admin/configuration/property_sets/property_set_form.html',
                controller: 'AdminConfigurationPropertySetFormController as PropertySetForm'
            })
            .state('admin.configuration.property_sets.new', {
                parent: 'admin.configuration',
                url: '/property_sets/new',
                templateUrl: 'states/admin/configuration/property_sets/property_set_form.html',
                controller: 'AdminConfigurationPropertySetFormController as PropertySetForm'
            })
}

export default angular.module('configuration.property_sets', [])
        .config(propertySetsConfig)
        .controller('AdminConfigurationPropertySetsController', AdminConfigurationPropertySetsController)
        .controller('AdminConfigurationPropertySetFormController', AdminConfigurationPropertySetFormController)
        .factory('PropertyFormModal', PropertyFormModalFactory)