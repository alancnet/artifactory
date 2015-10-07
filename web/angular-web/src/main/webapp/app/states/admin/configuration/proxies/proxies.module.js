import {AdminConfigurationProxiesController} from './proxies.controller';
import {AdminConfigurationProxyFormController} from './proxy_form.controller';

function proxiesConfig($stateProvider) {
    $stateProvider
            .state('admin.configuration.proxies', {
                params: {feature: 'Proxies'},
                url: '/proxies',
                templateUrl: 'states/admin/configuration/proxies/proxies.html',
                controller: 'AdminConfigurationProxiesController as AdminConfigurationProxies'
            })
            .state('admin.configuration.proxies.new', {
                params: {feature: 'Proxies'},
                parent: 'admin.configuration',
                url: '/proxies/new',
                templateUrl: 'states/admin/configuration/proxies/proxy_form.html',
                controller: 'AdminConfigurationProxyFormController as ProxyForm'
            })
            .state('admin.configuration.proxies.edit', {
                params: {feature: 'Proxies'},
                parent: 'admin.configuration',
                url: '/proxies/:proxyKey/edit',
                templateUrl: 'states/admin/configuration/proxies/proxy_form.html',
                controller: 'AdminConfigurationProxyFormController as ProxyForm'
            })
}

export default angular.module('configuration.proxies', [])
        .config(proxiesConfig)
        .controller('AdminConfigurationProxiesController', AdminConfigurationProxiesController)
        .controller('AdminConfigurationProxyFormController', AdminConfigurationProxyFormController);