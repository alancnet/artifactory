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

dojo.require("dijit.Menu");

dojo.declare('artifactory.Menu', dijit.Menu, {
    selfCleanup:true,

    templateString:'<div class="tree-actions">' +
            '<div class="links-border">' +
            '<div class="links-top"></div>' +
            '<div class="links-bottom"></div>' +
            '<div class="links-left"></div>' +
            '<div class="links-right"></div>' +
            '<div class="links-top-left"></div>' +
            '<div class="links-top-right"></div>' +
            '<div class="links-bottom-left"></div>' +
            '<div class="links-bottom-right"></div>' +
            '<div class="links-body">' +
            '<table class="dijit dijitMenu dijitReset dijitMenuTable" waiRole="menu" dojoAttachEvent="onkeypress:_onKeyPress">' +
            '<tbody class="dijitReset" dojoAttachPoint="containerNode"></tbody>' +
            '</table>' +
            '</div></div>' +
            '<div class="links-pointer"></div>' +
            '</div>',

    bindDomNode:function () {
    },

    unBindDomNode:function () {
    },

    onOpen:function () {
        this.inherited('onOpen', arguments);

        // position menu
        this.domNode.style.left = '-' + this.domNode.clientWidth + 'px';
        this.domNode.style.top = '-' + this.domNode.clientHeight / 2 + 'px';
    },

    onClose:function () {
        this.inherited('onClose', arguments);
        DomUtils.removeStyle(dojo.byId(this.sourceId), 'context-menu');
    },

    showWithLastEvent:function () {
        // restore last context menu event
        var e;
        if (dojo.isIE) {
            e = {target:window.lastTarget};
        } else {
            e = window.lastEvent;
            this._contextMenuWithMouse = true;
        }

        // show context menu
        this._openMyself(e);
        DomUtils.addStyle(dojo.byId(this.sourceId), 'context-menu');
    },

    destroy:function () {
        // cleanup
        dijit.popup.close(this);

        if (this.domNode) {
            var parent = this.domNode.parentNode;
            if (parent) {
                parent.removeChild(this.domNode);
            }
        }

        // call super
        this.inherited('destroy', arguments);
    }
});

dojo.declare("artifactory.MenuItem", dijit.MenuItem, {
    selfCleanup:true
});

var ActionsMenuPanel = {
    show:function (id) {
        var menu = dijit.byId('contextMenu');
        menu.sourceId = id;
        menu.showWithLastEvent();
    },

    init:function () {
        // cleanup last menu
        if (ActionsMenuPanel.widgets) {
            dojo.forEach(ActionsMenuPanel.widgets, function (widget) {
                widget.destroy();
            });
        }

        // create new menu
        ActionsMenuPanel.widgets = dojo.parser.parse(dojo.byId('contextMenu').parentNode);
    }
};
