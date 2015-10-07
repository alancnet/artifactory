var path = require('path');
var webpack = require('webpack');
var CommonsChunkPlugin = require('./node_modules/webpack/lib/optimize/CommonsChunkPlugin');
var CONFIG = require('./artifactory.config');

module.exports = {

    context: __dirname + '/app',
    entry: {
        artifactory_main: './app.js',
        artifactory_services: './services/artifactory.services.module.js',
        artifactory_directives: './directives/artifactory.directives.module.js',
        artifactory_dao: './data/artifactory_dao_module',
        artifactory_ui_components: './ui_components/ui_components.module',
        artifactory_states: './states/artifactory.states.module',
        artifactory_filters: './filters/artifactory.filters.module'
    },

    output: {
        path: CONFIG.DESTINATIONS.TARGET,
        filename: '[name].js'
    },

    plugins: [

        new CommonsChunkPlugin({
            name: "artifactory_core",
            filename: "artifactory_core.js",
            chunks: ["artifactory_services", "artifactory_dao"]
        }),

        new CommonsChunkPlugin({
            name: "artifactory_ui",
            filename: "artifactory_ui.js",
            chunks: ["artifactory_directives", "artifactory_ui_components", "artifactory_filters"]
        }),

        new CommonsChunkPlugin({
            name: "artifactory_views",
            filename: "artifactory_views.js",
            chunks: ["artifactory_states"]
        })

    ],

    module: {
        loaders: [{test: /\.js$/, loader: 'babel'}]
    },

    devtool: "#source-map"
};