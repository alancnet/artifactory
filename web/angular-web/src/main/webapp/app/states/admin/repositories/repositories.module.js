import {AdminRepositoriesController} from './repositories.controller';
import {AdminRepositoryFormController} from './repository_form.controller';
import {VirtualRepositoryFormController} from './virtual_repository_form.controller';
import {AdminRepositoriesLayoutController} from './repositories_layouts.controller';
import {AdminRepositoryLayoutFormController} from './repository_layout_form.controller';

function repositoriesConfig($stateProvider) {
    $stateProvider
        // base state
        .state('admin.repositories', {
            url: '',
            abstract: true,
            template: '<ui-view></ui-view>'
        })

        // repository list and forms
        .state('admin.repositories.list', {
            url: '/repositories/{repoType}',
            templateUrl: 'states/admin/repositories/repositories.html',
            controller: 'AdminRepositoriesController as Repositories'
        })
        .state('admin.repositories.list.edit', {
            parent: 'admin.repositories',
            url: '/repository/{repoType}/{repoKey}/edit',
            templateUrl: 'states/admin/repositories/repository_form.html',
            controller: 'AdminRepositoryFormController as RepositoryForm'
        })
        .state('admin.repositories.list.new', {
            parent: 'admin.repositories',
            url: '/repository/{repoType}/new',
            templateUrl: 'states/admin/repositories/repository_form.html',
            controller: 'AdminRepositoryFormController as RepositoryForm'
        })

        // repository layout list and forms
        .state('admin.repositories.repo_layouts', {
            url: '/repo_layouts',
            templateUrl: 'states/admin/repositories/repositories_layouts.html',
            controller: 'AdminRepositoriesLayoutController as RepositoriesLayoutController'
        })
        .state('admin.repositories.repo_layouts.edit', {
            parent: 'admin.repositories',
            url: '/repo_layouts/{layoutname}/edit',
            templateUrl: 'states/admin/repositories/repository_layout_form.html',
            controller: 'AdminRepositoryLayoutFormController as RepositoryLayoutForm',
            params: {viewOnly: true}
        })
        .state('admin.repositories.repo_layouts.new', {
            parent: 'admin.repositories',
            url: '/repo_layouts/new?copyFrom',
            templateUrl: 'states/admin/repositories/repository_layout_form.html',
            controller: 'AdminRepositoryLayoutFormController as RepositoryLayoutForm'
        })

}

export default angular.module('admin.repositories', [])
    .config(repositoriesConfig)
    .controller('AdminRepositoriesController', AdminRepositoriesController)
    .controller('AdminRepositoryFormController', AdminRepositoryFormController)
    .controller('VirtualRepositoryFormController', VirtualRepositoryFormController)
    .controller('AdminRepositoriesLayoutController', AdminRepositoriesLayoutController)
    .controller('AdminRepositoryLayoutFormController', AdminRepositoryLayoutFormController);



