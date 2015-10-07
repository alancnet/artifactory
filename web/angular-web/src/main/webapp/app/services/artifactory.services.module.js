import ApiConstants                             from '../constants/api.constants';
import {ArtifactoryNotifications}               from './artifactory_notifications';
import {ArtifactoryCookies}                     from './artifactory_cookies';
import {ArtifactoryHttpClient}                  from './artifactory_http_client';
import {ArtifactoryStorage}                     from './artifactory_storage';
import {ArtifactoryEventBus}                    from './artifactory_eventBus';
import {ArtifactoryXmlParser}                   from './artifactory_xml_parser';
import {UserFactory}                            from './user';
import {artifactoryDownload}                    from './artifactory_download';
import {ArtifactoryState}                       from './artifactory_state';
import {artifactorySessionInterceptor}          from './artifactory_session_interceptor';
import {artifactoryDebugInterceptor}            from './artifactory_debug_interceptor';
import {artifactorySpinnerInterceptor}          from './artifactory_spinner_interceptor';
import {artifactoryMessageInterceptor}          from './artifactory_message_interceptor';
import {artifactoryServerErrorInterceptor}      from './artifactory_server_error_interceptor';
import {ArtifactoryFeatures}                    from './artifactory_features';
import {NativeBrowser}                          from './native_browser';
import {ArtifactActions}                        from './artifact_actions';
import {parseUrl}                               from './parse_url';

angular.module('artifactory.services', ['ui.router', 'artifactory.ui_components', 'toaster'])
        .constant('RESOURCE', ApiConstants)
        .service('ArtifactoryCookies', ArtifactoryCookies)
        .service('ArtifactoryNotifications', ArtifactoryNotifications)
        .service('ArtifactoryHttpClient', ArtifactoryHttpClient)
        .service('ArtifactoryStorage', ArtifactoryStorage)
        .service('ArtifactoryEventBus', ArtifactoryEventBus)
        .service('ArtifactoryXmlParser', ArtifactoryXmlParser)
        .service('User', UserFactory)
        .service('ArtifactoryState', ArtifactoryState)
        .factory('artifactoryDownload', artifactoryDownload)
        .factory('artifactorySessionInterceptor', artifactorySessionInterceptor)
        .factory('artifactoryDebugInterceptor', artifactoryDebugInterceptor)
        .factory('artifactoryMessageInterceptor', artifactoryMessageInterceptor)
        .factory('artifactoryServerErrorInterceptor', artifactoryServerErrorInterceptor)
        .factory('artifactorySpinnerInterceptor', artifactorySpinnerInterceptor)
        .service('NativeBrowser', NativeBrowser)
        .service('ArtifactoryFeatures', ArtifactoryFeatures)
        .service('ArtifactActions', ArtifactActions)
        .factory('parseUrl', parseUrl)