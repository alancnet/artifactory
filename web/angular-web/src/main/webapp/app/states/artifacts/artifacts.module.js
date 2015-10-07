import EVENTS   from '../../constants/artifacts_events.constants';
import {ArtifactsController} from './artifacts/artifacts.controller';
import {BrowsersController} from './browsers/browsers.controller';
import {SearchController} from './search/search.controller';
import {jfTreeSearch}        from './jf_tree_search/jf_tree_search';
import {jfTreeBrowser}   from './jf_tree_browser/jf_tree_browser';
import {jfStashBrowser}   from './jf_stash_browser/jf_stash_browser';
import {jfSimpleBrowser}   from './jf_simple_browser/jf_simple_browser';
import {selectTargetPathFactory}   from './services/select_target_path';
import {commonGridColumns}   from './services/common_grid_columns';
import {selectDeleteVersionsFactory}   from './services/select_delete_versions';
import {jfArtifactInfo}  from './jf_artifact_info/jf_artifact_info';
import infoTabs          from './jf_artifact_info/info_tabs/info_tabs.module';

function artifactsConfig($stateProvider, $urlMatcherFactoryProvider) {

    $urlMatcherFactoryProvider.type('pathParam', {
        encode: function (item) {
            return item;
        },
        decode: function (item) {
            return item;
        },
        is: function (item) {
            return true;
        }
    });
    $stateProvider
            .state('artifacts', {
                url: '/artifacts',
                parent: 'app-layout',
                abstract: true,
                templateUrl: 'states/artifacts/artifacts.html',
                controller: 'ArtifactsController as Artifact',
                onExit: (ArtifactoryState, TreeBrowserDao) => {
                    // Stop saving last state of tree
                    ArtifactoryState.removeState('lastTreeState');
                    // Invalidate the tree node cache
                    TreeBrowserDao.invalidateRoots();
                },
                onEnter: (ArtifactoryState) => {
                    // This should be true only when going from tree to simple before selecting any node in the tree
                    ArtifactoryState.setState('tree_touched', false);
                }
            })
            .state('artifacts.browsers', {
                url: '/browse/{browser}',
                templateUrl: 'states/artifacts/browsers/browsers.html',
                controller: 'BrowsersController as Browsers',
                onEnter: (ArtifactoryStorage, $stateParams) => {
                    if ($stateParams.browser !== 'stash') ArtifactoryStorage.setItem('BROWSER', $stateParams.browser);
                }
            })
            .state('artifacts.browsers.search', {
                url: '/search/{searchType}/{params}',
                params: {searchParams: {}},
                onEnter: (ArtifactoryEventBus, $stateParams) => {
                    ArtifactoryEventBus.dispatch(EVENTS.SEARCH_URL_CHANGED, $stateParams);
                },
                views: {
                    'search@artifacts': {
                        templateUrl: 'states/artifacts/search/search.html',
                        controller: 'SearchController as Search'
                    }
                }

            })
            .state('artifacts.browsers.path', {
                url: '/{tab}/{artifact:pathParam}',
                onEnter: (ArtifactoryEventBus, $stateParams, ArtifactoryState) => {
                    // Save state of tree
                    ArtifactoryState.setState('lastTreeState', {name: 'artifacts.browsers.path', params: $stateParams});
                    ArtifactoryEventBus.dispatch(EVENTS.ARTIFACT_URL_CHANGED, $stateParams);
                },
                params: {
                    forceLoad: false // used to force reload of state even if it's the same artifact
                }
            })
}

export default angular.module('artifacts', ['infoTabs'])
        .config(artifactsConfig)
        .controller('ArtifactsController', ArtifactsController)
        .controller('BrowsersController', BrowsersController)
        .controller('SearchController', SearchController)
        .directive('jfTreeSearch', jfTreeSearch)
        .directive('jfTreeBrowser', jfTreeBrowser)
        .directive('jfSimpleBrowser', jfSimpleBrowser)
        .directive('jfStashBrowser', jfStashBrowser)
        .directive('jfArtifactInfo', jfArtifactInfo)
        .factory('selectTargetPath', selectTargetPathFactory)
        .factory('commonGridColumns', commonGridColumns)
        .factory('selectDeleteVersions', selectDeleteVersionsFactory)
