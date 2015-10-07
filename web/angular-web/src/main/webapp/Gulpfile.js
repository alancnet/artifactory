var gulp = require("gulp");
var gutil = require("gulp-util");
var webpack = require("webpack");
var path = require('path');
var concat = require('gulp-concat');
var html2js = require('gulp-html2js');
var webpackConfig = require('./webpack.config');
var less = require('gulp-less');
var sourceMaps = require('gulp-sourcemaps');
var CONFIG = require('./artifactory.config');
var install = require("gulp-install");
var iconfont = require('gulp-iconfont');
var iconfontCss = require('gulp-iconfont-css');
var browserSync = require('browser-sync');
var runSequence = require('run-sequence');
var reload      = browserSync.reload;
var webserver = require('gulp-webserver');
var prefixer = require('gulp-autoprefixer');
var combiner = require('stream-combiner2');
var uglify = require('gulp-uglify');
var minifyCss = require('gulp-minify-css');
var RevAll = require('gulp-rev-all');
var revReplace = require("gulp-rev-replace");
var revNapkin = require('gulp-rev-napkin');

var rimraf = require('gulp-rimraf');

// default task runs the development tasks seq
gulp.task('default',['build', 'watch']);


// Build tasks common to dev and production
gulp.task('build:common',
    function(callback) {
        runSequence(
            'clean',
            [
                'webpack',
                'templates',
                'vendorScripts',
                'vendorStyles',
                'vendorStylesAssets',
                'vendorFonts',
                'less',
                'copyHtml',
                'fonts',
                'images'
            ],
            callback
        );
    }
);


// Build the client. This is run by Jenkins
gulp.task('build',
    function(callback) {
        delete webpackConfig.devtool; //don't generate source maps on production build
        runSequence(
            'build:common',
            'revreplace',
            callback
        );
    }
);

// Same as build:common just copy styleguide
gulp.task("build:dev", 
    function(callback) {
        runSequence(
            'build:common',
            'copyStyleguide',
            callback
        );
    }
);

// Clean up
gulp.task('clean', function() { 
    return gulp.src(CONFIG.DESTINATIONS.TARGET, { read: false })
        .pipe(rimraf({ force: true }));
});

// Reload everything:
gulp.task("reload", reload);
// Reload CSS files:
gulp.task("reloadCss", function() {
    reload(["vendorStyles.css", "application.css"]);
});

// Utility factory function to create a function that runs a sequence
function sequence() {
    var args = arguments;
    return function() {
        runSequence.apply(this, args);
    }
}

// Run browserSync that proxies to Artifactory REST Server
gulp.task("serve:dev", ["build:dev", "watch:dev"], connectTo('http://10.100.1.110:8081'));
gulp.task("serve:local", ["build:dev", "watch:dev"], connectTo('http://localhost:8081'));
gulp.task("serve:8080", ["build:dev", "watch:dev"], connectTo('http://localhost:8080'));
gulp.task("serve", ["serve:local"]);

function connectTo(url) {
    return function(cb) {
        gulp.src(CONFIG.DESTINATIONS.TARGET)
        .pipe(webserver({
          open: false,
          proxies: [
            {source: '/artifactory/ui', target: url + '/artifactory/ui'},
            {source: '/artifactory/webapp', target: 'http://localhost:8000'}
          ]
        }));
        browserSync({
            proxy: 'localhost:8000',
            ghostMode: false,
            open: false
        });
        return cb;
    };
}

// Set watchers and run relevant tasks - then reload (when running under browsersync)
gulp.task('watch', function () {
    gulp.watch('./bower.json', sequence('bower', ['vendorScripts', 'vendorStyles', 'vendorStylesAssets', 'vendorFonts'], 'reload'));
    gulp.watch(CONFIG.SOURCES.APPLICATION_JS, sequence('webpack', 'reload'));
    gulp.watch(CONFIG.SOURCES.TEMPLATES, sequence('templates', 'reload'));
    gulp.watch(CONFIG.SOURCES.REQUIRED_TEMPLATES, sequence('webpack', 'reload'));
    gulp.watch(CONFIG.SOURCES.LESS, sequence('less', 'reloadCss'));
    gulp.watch(CONFIG.SOURCES.VENDOR_JS, sequence(['vendorScripts', 'vendorStyles', 'vendorStylesAssets', 'vendorFonts'], 'reload'));
    gulp.watch(CONFIG.SOURCES.VENDOR_CSS, sequence(['vendorStyles'], 'reloadCss'));
    gulp.watch(CONFIG.SOURCES.FONTS, sequence('fonts', 'reload'));
    gulp.watch(CONFIG.SOURCES.INDEX, sequence('copyHtml', 'reload'));
});

// Watch the styleguide
gulp.task('watch:dev', ["watch"], function () {
    gulp.watch(CONFIG.SOURCES.STYLEGUIDE, sequence('copyStyleguide', 'reload'));
});

// install bower dependedencies
gulp.task('bower', function () {
    return gulp.src(['./bower.json'])
            .pipe(install());
});

// bundle application code
gulp.task("webpack", function (callback) {
    return webpack(webpackConfig, function (err, stats) {
        console.log(err);
        if (err) {
            throw new gutil.PluginError("webpack", err)
        }
        gutil.log("[webpack]", stats.toString({
            // output options
        }));
        callback();
    });
});

// cache templates
gulp.task('templates', function () {
    return gulp.src(CONFIG.SOURCES.TEMPLATES)
            .pipe(html2js({
                outputModuleName: 'artifactory.templates',
                base: 'app/',
                useStrict: true
            }))
            .pipe(concat('templates.js'))
            .pipe(gulp.dest(CONFIG.DESTINATIONS.TARGET))
});

// concat vendor scripts
gulp.task('vendorScripts', function () {
    return gulp.src(CONFIG.SOURCES.VENDOR_SCRIPTS)
            .pipe(concat('vendorScripts.js'))
            // .pipe(uglify())
            .pipe(gulp.dest(CONFIG.DESTINATIONS.TARGET));
});

// concat vendor stylesheets
gulp.task('vendorStyles', function () {
   return gulp.src(CONFIG.SOURCES.VENDOR_CSS)
            .pipe(concat('vendorStyles.css'))
            // .pipe(minifyCss())
            .pipe(gulp.dest(CONFIG.DESTINATIONS.TARGET + '/css'));
});

// copy vendor assets to css
gulp.task('vendorStylesAssets', function () {
    return gulp.src(CONFIG.SOURCES.VENDOR_ASSETS)
            .pipe(gulp.dest(CONFIG.DESTINATIONS.TARGET + '/css'));
});

gulp.task('vendorFonts', function () {
    return gulp.src(CONFIG.SOURCES.VENDOR_FONTS)
            .pipe(gulp.dest(CONFIG.DESTINATIONS.TARGET + '/fonts'));
});

gulp.task('iconfonts', function(){
    return gulp.src(CONFIG.SOURCES.MEDIUM_SVG_ICONS)
            .pipe(iconfontCss({
                fontName: 'medium_svgicons'
            }))
            .pipe(iconfont({
                fontName: 'medium_svgicons'
            }))
            .pipe(gulp.dest(CONFIG.DESTINATIONS.TARGET + '/css'));
});


// compile less
gulp.task('less', function () {
    var combined = combiner.obj([
        gulp.src(CONFIG.SOURCES.LESS_MAIN_FILE),
        sourceMaps.init(),
        less({paths: [path.join(__dirname, 'less', 'includes')]}),
        prefixer(),
        concat('application.css'),
        sourceMaps.write(),
        gulp.dest(CONFIG.DESTINATIONS.TARGET + '/css')
    ]);

    // any errors in the above streams will get caught
    // by this listener, instead of being thrown:
    combined.on('error', console.error.bind(console));

    return combined;
});

// copy html file to dest
gulp.task('copyHtml', function () {
    return gulp.src(CONFIG.SOURCES.INDEX)
        .pipe(gulp.dest(CONFIG.DESTINATIONS.TARGET))
});

// copy styleguide file to dest - for development only
gulp.task('copyStyleguide', function () {
    return gulp.src(CONFIG.SOURCES.STYLEGUIDE)
        .pipe(gulp.dest(CONFIG.DESTINATIONS.TARGET))
});

//copy fonts
gulp.task('fonts', function () {
    return gulp.src(CONFIG.SOURCES.FONTS)
            .pipe(gulp.dest(CONFIG.DESTINATIONS.TARGET + '/fonts'))
});

//copy images
gulp.task('images', function () {
    return gulp.src(CONFIG.SOURCES.IMAGES)
            .pipe(gulp.dest(CONFIG.DESTINATIONS.TARGET + '/images'))
});

function transformFilename(file, hash) {
    var ext = path.extname(file.path);
    return path.basename(file.path, ext) + '.' + process.env.BUILD_NUMBER + ext; // filename.<BUILD_NUMBER>.ext
}

gulp.task("revision", function(){
    if (process.env.BUILD_NUMBER) {    
        var revAll = new RevAll({transformFilename: transformFilename});
        return gulp.src(CONFIG.DESTINATIONS.TARGET_REV)
            .pipe(revAll.revision())
            .pipe(revNapkin({force:true}))
            .pipe(gulp.dest(CONFIG.DESTINATIONS.TARGET))
            .pipe(revAll.manifestFile())
            //     .pipe(revDel({ dest: CONFIG.DESTINATIONS.TARGET, force: true }))
            .pipe(gulp.dest(CONFIG.DESTINATIONS.TARGET))
    }
})

gulp.task("revreplace", ['revision'], function() {
    if (process.env.BUILD_NUMBER) {    
        var manifest = gulp.src(CONFIG.DESTINATIONS.TARGET + "/rev-manifest.json");
        return gulp.src(CONFIG.SOURCES.INDEX)
            .pipe(revReplace({manifest: manifest}))
            .pipe(gulp.dest(CONFIG.DESTINATIONS.TARGET))
    }
});