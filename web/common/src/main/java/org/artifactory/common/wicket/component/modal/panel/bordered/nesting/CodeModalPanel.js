if (!window.ModelCode) {
    var ModelCode = {
        onShow: function (id) {
            var node = dojo.byId(id);
            var lines = dojo.query('.syntaxhighlighter .lines', node)[0];

            // bindModalHeight
            ModalHandler.bindModalHeight(lines || node);

            // fix width
            if (lines) {
                ModelCode.setLinesWidth(Math.max(lines.scrollWidth, lines.clientWidth) + 'px');
            }
        },

        onClose: function() {
            ModelCode.setLinesWidth('auto');
        },

        setLinesWidth: function(width) {
            var style = '.modal-code .syntaxhighlighter .line {width: ' + width + ' !important;}';
            if (Wicket.Browser.isIE()) {
                ModelCode.style.cssText = style;
            } else {
                ModelCode.style.innerHTML = style;
            }
        }
    };

    // setup style tag
    if (Wicket.Browser.isIE()) {
        ModelCode.style = document.createStyleSheet();
    } else {
        ModelCode.style = document.createElement("style");
        document.getElementsByTagName("head")[0].appendChild(ModelCode.style);
    }
}