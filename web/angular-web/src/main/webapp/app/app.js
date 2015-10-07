import Models                   from './models/models.module';

// For debugging:
window._inject = function(injectable) {
    return angular.element(document.body).injector().get(injectable);
}

if (!String.prototype.endsWith) {
    String.prototype.endsWith = function (str) {
        return this.substr(this.length - str.length,str.length)===str;
    }
}
if (!String.prototype.startsWith) {
    String.prototype.startsWith = function (str) {
        return this.substr(0, str.length)===str;
    }
}

/**
 * providers configurations
 * @param $urlRouterProvider
 */
function appConfig($stateProvider, $urlRouterProvider, ngClipProvider, $httpProvider) {
    $urlRouterProvider.otherwise('/home');
    ngClipProvider.setPath("css/ZeroClipboard.swf");
    $httpProvider.interceptors.push('artifactorySpinnerInterceptor');
    $httpProvider.interceptors.push('artifactoryMessageInterceptor');
    $httpProvider.interceptors.push('artifactorySessionInterceptor');
    $httpProvider.interceptors.push('artifactoryServerErrorInterceptor');
    $httpProvider.interceptors.push('artifactoryDebugInterceptor');
}

function appRun($httpBackend, $rootScope) {
    $httpBackend.whenPOST(/.*/).passThrough();
    $httpBackend.whenPUT(/.*/).passThrough();
    $httpBackend.whenGET(/.*/).passThrough();
    $httpBackend.whenDELETE(/.*/).passThrough();
    $httpBackend.whenPATCH(/.*/).passThrough();
    defineCodeMirrorMimes();
    defineCodeMirrorLinkOverlay();
}

angular.module('artifactory.ui', [

    // Vendor modules
    'ngMessages',
    'ui.utils',
    'ui.select',
    'selectize',
    'ui.codemirror',
    'cfp.hotkeys',
    'artifactory.templates',
    'ui.router',
/* --- MOVED TO artifactory_grid module
    'ui.grid',
    'ui.grid.autoResize',
    'ui.grid.edit',
    'ui.grid.selection',
    'ui.grid.pagination',
    'ui.grid.grouping',
    'ui.grid.resizeColumns',
*/
    'ngCookies',
    'toaster',
    'ngSanitize',
    'ui.layout',
    'ui.bootstrap',
    'ngMockE2E',
    'ngClipboard',
    'ngPasswordStrength',
    'angular-capitalize-filter',
    'angularFileUpload',
    // Application modules
    'artifactory.services',
    'artifactory.directives',
    'artifactory.dao',
    'artifactory.ui_components',
    'artifactory.states',
    'artifactory.filters',
    'ui.grid.draggable-rows',
    'color.picker',
    Models.name
])
    .config(appConfig)
    .run(appRun);

function aliasMime(newMime, existingMime) {
    CodeMirror.defineMIME(newMime, CodeMirror.mimeModes[existingMime]);
}
function defineCodeMirrorMimes() {
    aliasMime('text/x-java-source', 'text/x-java');
    aliasMime('pom', 'text/xml');
}

function defineCodeMirrorLinkOverlay() {
    var urlRegex = /^https?:\/\/[a-zA-Z]+(\.)?(:[0-9]+)?.+?(?=\s|$|"|'|>|<)/;
    CodeMirror.defineMode("links", function (config, parserConfig) {
        var linkOverlay = {
            token: function (stream, state) {
                if (stream.match(urlRegex)) {
                    return "link";
                }
                while (stream.next() != null && !stream.match(urlRegex, false)) {
                }
                return null;
            }
        };

        return CodeMirror.overlayMode(CodeMirror.getMode(config, config.mimeType || "text/xml"), linkOverlay);
    });
}