(function(sh) {
    // RTFACT-3035: patch 'auto-links: true' (copied from shCore.js)
    sh.utils.processUrls = function(code) {
        var lt = '&lt;', gt = '&gt;';

        return code.replace(sh.regexLib.url, function(m) {
            var suffix = '', prefix = '';
            if (m.indexOf(lt) == 0) {
                prefix = lt;
                m = m.substring(lt.length);
            }

            if (m.indexOf(gt) == m.length - gt.length) {
                m = m.substring(0, m.length - gt.length);
                suffix = gt;
            }

            // added by yoava
            var ltIndex = m.lastIndexOf('&lt;/');
            if (!suffix && ltIndex > 0) {
                suffix = m.substring(ltIndex);
                m = m.substring(0, ltIndex);
            }
            return prefix + '<a href="' + m + '">' + m + '</a>' + suffix;
        });
    };

    sh.hasFlash = function() {
        var stdPlugin = (navigator.mimeTypes
                && navigator.mimeTypes["application/x-shockwave-flash"]
                && navigator.mimeTypes["application/x-shockwave-flash"].enabledPlugin)
                || navigator.plugins["Shockwave Flash"]
                || navigator.plugins["Shockwave Flash 2.0"];
        if (stdPlugin) {
            return true;
        }
        if (window.ActiveXObject) {
            try {
                return new ActiveXObject("ShockwaveFlash.ShockwaveFlash");
            } catch (e) {
            }
        }
        return false;
    };

    sh.byId = function(id, clipboardSwf, brush, gutter, toolbar, autoLinks, wrapLines) {
        // rebuild brushes if needed (more brushes might be added with ajax
        var brushes = sh.vars.discoveredBrushes;
        if (brushes && !brushes[brush]) {
            sh.vars.discoveredBrushes = null;
        }

        // config copy swf
        if (sh.hasFlash()) {
            sh.config.clipboardSwf = clipboardSwf;
        }

        sh.highlight({
            brush: brush,
            gutter:gutter,
            toolbar:toolbar,
            'auto-links':autoLinks ,
            'wrap-lines':wrapLines
        }, document.getElementById('code-' + id));
    };
})(SyntaxHighlighter);
