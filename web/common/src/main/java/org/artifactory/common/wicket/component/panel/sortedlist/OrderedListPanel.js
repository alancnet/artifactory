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

dojo.provide("artifactory.OrderedListPanel");

dojo.declare('OrderedListPanel', artifactory.Selectable, {
    init:function (listId, textFieldId, upLinkId, downLinkId) {
        this.domNode = dojo.byId(listId);
        this.textField = dojo.byId(textFieldId);
        this.upLink = dojo.byId(upLinkId);
        this.downLink = dojo.byId(downLinkId);
        this.domNode.widget = this;

        this.initSourceNode(this.domNode, function (item, i) {
            item.idx = i;
        });
        this.updateIndices(true);
    },

    onLoad:function () {
        var me = this;

        this.upLink.onclick = function () {
            me.moveUp();
            return false;
        };

        this.downLink.onclick = function () {
            me.moveDown();
            return false;
        };
    },

    updateIndices:function (silent) {
        var textfield = this.textField;
        var prevValue = textfield.value;

        // update indices list
        var value = '';
        dojo.forEach(this.domNode.childNodes, function (item, i) {
            if (i % 2) {
                item.className = item.className.replace(/even/, 'odd');
            } else {
                item.className = item.className.replace(/odd/, 'even');
            }
            value += ',' + item.idx;
        });
        textfield.value = value.substring(1);

        // check if order changed
        if (!silent && prevValue != textfield.value) {
            // needed for preserveState()
            this.domNode.sourceWidget.updateIndices();

            // trigger event
            eval(textfield.getAttribute('onOrderChanged'));
        }
    },

    updateMoveButtons:function () {
        var source = this.domNode.sourceWidget;
        var anySelected = source.isAnySelected();
        var firstSelected = source.selection[this.domNode.firstChild.id];
        var lastSelected = source.selection[this.domNode.lastChild.id];
        this.setButtonEnabled(this.downLink, 'down-link', anySelected && !lastSelected);
        this.setButtonEnabled(this.upLink, 'up-link', anySelected && !firstSelected);
    },

    moveUp:function () {
        this.domNode.sourceWidget.forEachSelected(function (item) {
            item.parentNode.insertBefore(item, item.previousSibling);
        });
        this.updateMoveButtons();
        this.updateIndices();
    },

    moveDown:function () {
        this.domNode.sourceWidget.forEachSelectedReverse(function (item) {
            item.parentNode.insertBefore(item.nextSibling, item);
        });
        this.updateMoveButtons();
        this.updateIndices();
    },

    onSelection:function () {
        this.updateMoveButtons();
    },

    onDrop:function (source, target) {
        if (source.parent == this.domNode || target.parent == this.domNode) {
            this.updateMoveButtons();
            this.updateIndices();
        }
    },

    destroy:function () {
        this.inherited(arguments);

        this.upLink.onclick = undefined;
        this.downLink.onclick = undefined;

        delete this.domNode.widget;
        delete this.domNode;
        delete this.textField;
        delete this.upLink;
        delete this.downLink;
    }
});
