import {AllBuildsController} from './all_builds.controller';
import {BuildsInfoController} from './builds_info/builds_info.controller';
import {BuildsHistoryController} from './builds_history/builds_history.controller';
import {BuildsLocateController} from './builds_locate/builds_locate.controller';
import buildTabs          from './builds_info/build_tabs/build_tabs.module';

function buildsConfig($stateProvider) {
    $stateProvider
            .state('builds', {
                url: '/builds',
                parent: 'app-layout',
                abstract: true,
                templateUrl: 'states/builds/builds.html'
            })
            .state('builds.all', {
                url: '/',
                templateUrl: 'states/builds/all_builds.html',
                controller: 'AllBuildsController as AllBuilds'
            })
            .state('builds.history', {
                url: '/{buildName}',
                templateUrl: 'states/builds/builds_history/builds_history.html',
                controller: 'BuildsHistoryController as BuildsHistory'
            })
            .state('builds.locate', {
                url: '/{buildName}/{buildNumber}',
                templateUrl: 'states/builds/builds_locate/builds_locate.html',
                controller: 'BuildsLocateController as BuildsLocate'
            })
            .state('builds.info', {
                url: '/{buildName}/{buildNumber}/{startTime}/{tab}/{moduleID}',
                templateUrl: 'states/builds/builds_info/builds_info.html',
                controller: 'BuildsInfoController as BuildsInfo'
            })
}

export default angular.module('builds', ['buildTabs'])
        .config(buildsConfig)
        .controller('AllBuildsController', AllBuildsController)
        .controller('BuildsInfoController', BuildsInfoController)
        .controller('BuildsHistoryController', BuildsHistoryController)
        .controller('BuildsLocateController', BuildsLocateController)
