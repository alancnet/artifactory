var vendorPaths = require('./app/vendor');

module.exports = {

    SOURCES: {
        APPLICATION_JS: 'app/**/*.js',
        TEMPLATES: 'app/**/**/*.html',
        REQUIRED_TEMPLATES: 'app/ui_components/artifactory_grid/templates/*.html',
        VENDOR_SCRIPTS : vendorPaths.JS,
        VENDOR_CSS : vendorPaths.CSS,
        VENDOR_ASSETS: vendorPaths.ASSETS,
        VENDOR_FONTS: vendorPaths.FONTS,
        LESS: 'app/assets/stylesheets/**/*.less',
        LESS_MAIN_FILE: 'app/assets/stylesheets/main.less',
        INDEX : 'app/app.html',
        STYLEGUIDE: 'app/styleguide.html',
        FONTS : 'app/assets/fonts/**',
        VENDOR_JS : 'app/vendor.js',
        IMAGES : 'app/assets/images/**',
        MEDIUM_SVG_ICONS: 'app/assets/svgicons/*.svg'
    },

    DESTINATIONS: {
        TARGET: '../../../../war/src/main/webapp/webapp',
        TARGET_REV: [
            '../../../../war/src/main/webapp/webapp/**'
        ]
    }
};