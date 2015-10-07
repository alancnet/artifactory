/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

var ModalHandler = {
    onPopup:function () {
        ModalHandler.resizeCurrent();
        if (dojo.isIE <= 7) {
            setTimeout(function () {
                ModalHandler.resizeCurrent();
            }, 200);
        }

        var modal = Wicket.Window.current;
        modal.center();
        var activeElement = document.activeElement;
        if (activeElement && activeElement.blur && activeElement != document.body) {
            activeElement.blur();
        }

        // connect to onkeyup to catch ESC press
        if (!ModalHandler.connection) {
            ModalHandler.connection = dojo.connect(document, 'onkeyup', ModalHandler.onkeyup);
        }
    },

    onkeyup:function (e) {
        var event = e ? e : window.event;
        var modal = Wicket.Window.current;
        if (event.keyCode == 27 && modal && !modal.closing) {
            modal.close();
        }
    },

    onError:function () {
        var modal = Wicket.Window.current;
        if (!modal.wasResized) {
            window.setTimeout(ModalHandler.resizeCurrent, 100);
        }
    },
    onClose:function () {
        // disconnect from onkeyup
        if (!Wicket.Window.current) {
            dojo.disconnect(ModalHandler.connection);
            ModalHandler.connection = null;
        }
    },

    bindModalHeight:function (node) {
        var modal = Wicket.Window.current;
        if (!modal) {
            return;
        }

        ModalHandler.autoHeight(node);
        if (dojo.isIE <= 7) {
            setTimeout(function () {
                ModalHandler.autoHeight(node)
            }, 100);
        }
        modal.onresize = function () {
            ModalHandler.autoHeight(node);
        };
    },

    autoHeight:function (node) {
        var modal = Wicket.Window.current;
        if (!modal) {
            return;
        }

        var content = modal.content;
        var height = node.clientHeight + content.offsetHeight - content.firstChild.offsetHeight - 6;

        if (height > 0) {
            node.style.height = height + 'px';
            node.style.maxHeight = height + 'px';
        } else {
            node.style.height = 'auto';
            node.style.maxHeight = '';
        }
    },

    centerCurrent:function () {
        if (Wicket.Window.current) {
            Wicket.Window.current.center();
        }
    },

    resizeCurrent:function () {
        var modal = Wicket.Window.current;
        if (!modal) {
            return;
        }

        var width = modal.settings.width;
        var height = modal.settings.height;

        if (width == 0) {
            width = modal.content.firstChild.scrollWidth + 5;
        }
        if (height == 0) {
            height = modal.content.firstChild.scrollHeight + 25;
        }

        var maxWidth = Wicket.Window.getViewportWidth();
        var maxHeight = Wicket.Window.getViewportHeight() - 70;

        width = Math.min(width, maxWidth);
        height = Math.min(height, maxHeight);

        modal.window.style.width = width + modal.settings.widthUnit;
        modal.content.style.height = height + modal.settings.heightUnit;
        modal.onresize();
    },

    resizeAndCenter:function () {
        ModalHandler.resizeCurrent();
        ModalHandler.centerCurrent();
    }
};

Wicket.Window.prototype.onresize = function () {
};

Wicket.Window.prototype.resizing = function () {
    var modal = Wicket.Window.current;
    modal.wasResized = true;
    modal.onresize();
};

/**
 * Returns the modal window markup with specified element identifiers.
 */
Wicket.Window.getMarkup =
        function (idWindow, idClassElement, idCaption, idContent, idTop, idTopLeft, idTopRight, idLeft, idRight,
                idBottomLeft, idBottomRight, idBottom, idCaptionText, isFrame) {
            var s =
                    "<div class=\"wicket-modal\" id=\"" + idWindow +
                            "\" style=\"top: 10px; left: 10px; width: 100px;\">" +
                            "<div id=\"" + idClassElement + "\">" +

                            "<div class=\"w_top_1\">" +

                            "<div class=\"w_topLeft\" id=\"" + idTopLeft + "\">" +
                            "</div>" +

                            "<div class=\"w_topRight\" id=\"" + idTopRight + "\">" +
                            "</div>" +

                            "<div class=\"w_top\" id='" + idTop + "'>" +
                            "</div>" +

                            "</div>" +

                            "<div class=\"w_left\" id='" + idLeft + "'>" +
                            "<div class=\"w_right_1\">" +
                            "<div class=\"w_right\" id='" + idRight + "'>" +
                            "<div class=\"w_content_1\" onmousedown=\"if (Wicket.Browser.isSafari()) { event.ignore = true; }  else { Wicket.stopEvent(event); } \">" +
                            "<div class=\"w_caption\"  id=\"" + idCaption + "\">" +
                            "<a class=\"w_close\" href=\"#\"></a>" +
                            "<span id=\"" + idCaptionText + "\" class=\"w_captionText\"></span>" +
                            "</div>" +

                            "<div class=\"w_content_2\">" +
                            "<div class=\"w_content_3\">" +
                            "<div class=\"w_content\">";
            if (isFrame) {
                if (Wicket.Browser.isIELessThan7() || !Wicket.Browser.isIE()) {
                    s += "<iframe src='\/\/:' frameborder=\"0\" id='" + idContent +
                            "' allowtransparency=\"false\" style=\"height: 200px\">" +
                            "</iframe>";
                } else {
                    s += "<iframe src='about:blank' frameborder=\"0\" id='" + idContent +
                            "' allowtransparency=\"false\" style=\"height: 200px\">" +
                            "</iframe>";
                }
            } else {
                s +=
                        "<div id='" + idContent + "' class='w_content_scroll'></div>";
            }
            s +=
                    "</div>" +
                            "</div>" +
                            "</div>" +
                            "</div>" +
                            "</div>" +
                            "</div>" +
                            "</div>" +


                            "<div class=\"w_bottom_1\" id=\"" + idBottom + "\">" +

                            "<div class=\"w_bottomRight\"  id=\"" + idBottomRight + "\">" +
                            "</div>" +

                            "<div class=\"w_bottomLeft\" id=\"" + idBottomLeft + "\">" +
                            "</div>" +

                            "<div class=\"w_bottom\" id=\"" + idBottom + "\">" +
                            "</div>" +


                            "</div>" +


                            "</div>" +
                            "</div>";

            return s;
        };

Wicket.Window.unloadConfirmation = false

Wicket.Window.prototype.superClose = Wicket.Window.prototype.close;
Wicket.Window.prototype.checkedClose = function () {
    if (this.closing) {
        this.closing = false;
        this.superClose(true);
        this.settings.onClose();
    }
};

Wicket.Window.prototype.close = function () {
    var me = this;
    me.checkedClose();
    me.closing = true;

    dojo.fadeOut({
        node:me.window,
        duration:220,
        onEnd:function () {
            me.checkedClose();
        }
    }).play();
};
