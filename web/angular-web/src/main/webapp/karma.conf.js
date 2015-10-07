var browserStack = require('./browser_stack.config');
module.exports = function (config) {
    config.set({
        basePath: '',
        frameworks: ['jasmine', 'browserify'],
        files: [
            '../../../../war/src/main/webapp/webapp/vendorScripts*.js',
            '../../../../war/src/main/webapp/webapp/templates*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_core*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_services*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_dao*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_ui*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_directives*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_ui_components*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_views*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_filters*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_states*.js',
            '../../../../war/src/main/webapp/webapp/artifactory_main*.js',
            'specs/spec_helper.js',
            'components/jasmine-jquery/lib/jasmine-jquery.js',
            'mocks/**/**.js',
            'specs/**/**.js'
        ],
        exclude: [
            "open/web/war/src/main/webapp/webapp/css/**",
            "open/web/war/src/main/webapp/webapp/fonts/**"
        ],
        preprocessors: {
            '{specs,mocks}/**/**.js': [],
            '{specs,mocks}/**/**.browserify.js': ['browserify'],
            'mocks/tree_node_mock.browserify.js': ['browserify']
        },
        browserify: {
            debug: true,
            transform: ['babelify']
        },
        junitReporter: {
            outputDir: 'test_results'
        },
        reporters: ['progress'],
        port: 9876,
        colors: true,
        logLevel: config.LOG_INFO,
        autoWatch: true,

        browserStack: {
            project: 'Artifactory Karma',
            build: process.env.BUILD_NUMBER
        },

        browserDisconnectTimeout: 20000,
        browserDisconnectTolerance: 3,
        browserNoActivityTimeout: 60000,        

        // define browsers
        customLaunchers: browserStack.browsers,

        browsers: ['Chrome'],

        singleRun: false
    });
};
