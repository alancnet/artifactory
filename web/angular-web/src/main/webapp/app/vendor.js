/**
 * those files will be loaded and concat by gulp
 * @type {{JS: string[], CSS: string[]}}
 */
module.exports = {

    JS: [
        'components/angular/angular.js',
        'components/angular-mocks/angular-mocks.js',
        'components/angular-cookies/angular-cookies.js',
        'components/angular-sanitize/angular-sanitize.js',
        'components/angular-messages/angular-messages.js',
        'components/angular-ui-layout/ui-layout.js',
        'components/angular-ui-router/release/angular-ui-router.js',
        'components/angular-bootstrap/ui-bootstrap-tpls.js',
        'components/angular-ui-grid/ui-grid.js',
        'components/angular-resource/angular-resource.js',
        'components/angularjs-toaster/toaster.js',
        'components/jquery/dist/jquery.js',
        'components/jquery-ui/jquery-ui.js',
        'components/lodash/lodash.js',
        'components/codemirror/lib/codemirror.js',
        'components/codemirror/addon/mode/overlay.js',
        'components/codemirror/mode/xml/xml.js',
        'components/codemirror/mode/javascript/javascript.js',
        'components/codemirror/mode/clike/clike.js',
        'components/codemirror/addon/dialog/dialog.js',
        'components/codemirror/addon/search/searchcursor.js',
        'components/angular-ui-codemirror/ui-codemirror.js',
        'components/x2js/xml2json.js',
        'components/angular-hotkeys/build/hotkeys.js',
        'components/jstree/dist/jstree.js',
        'components/angular-ui-select/dist/select.js',
        'components/selectize/dist/js/standalone/selectize.js',
        'components/angular-selectize2/dist/selectize.js',
        'components/zeroclipboard/dist/ZeroClipboard.js',
        'components/ng-clip/src/ngClip.js',
        'components/angular-file-upload/angular-file-upload.js',
        'components/angular-ui-utils/ui-utils.js',
        'components/later/later.js',
        'vendor/jquery.highlight.js',
        'components/ng-password-strength/dist/scripts/ng-password-strength.js',
        'components/tooltipster/js/jquery.tooltipster.min.js',
        'components/angular-capitalize-filter/capitalize.js',
        'components/tinycolor/tinycolor.js',
        'components/angularjs-color-picker/angularjs-color-picker.min.js',
        'vendor/draggable-rows.js'
    ],

    CSS: [
        'components/jquery-ui/themes/smoothness/jquery-ui.css',
        'components/bootstrap/dist/css/bootstrap.css',
        'components/angular-ui-layout/ui-layout.css',
        'components/angular-ui-grid/ui-grid.css',
        'components/angularjs-color-picker/angularjs-color-picker.min.css',
        'components/animate.css/animate.css',
        'components/angularjs-toaster/toaster.css',
        'components/codemirror/lib/codemirror.css',
        'components/codemirror/lib/codemirror.css',
        'components/angular-hotkeys/build/hotkeys.css',
        'components/jstree/dist/themes/default/style.css',
        'components/angular-ui-select/dist/select.css',
        'components/lessfonts-open-sans/dist/css/open-sans.css',
        //'components/selectize/dist/css/selectize.css',
        'components/selectize/dist/css/selectize.bootstrap3.css',
        'components/font-awesome/css/font-awesome.css',
        'components/tooltipster/css/tooltipster.css',
        'components/codemirror/addon/dialog/dialog.css'
    ],

    FONTS: [
        'components/lessfonts-open-sans/dist/fonts/**/*.{svg,woff,ttf,eot}',
        'components/font-awesome/fonts/*.{svg,woff,ttf,eot}'
    ],

    ASSETS: [
        'components/jstree/dist/themes/default/*.{png,gif}',
        'components/angular-ui-grid/*.{svg,woff,ttf,eot}',
        'components/zeroclipboard/dist/ZeroClipboard.swf'
    ]
};