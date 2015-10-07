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

dojo.require("dojo.dnd.Source");

/**
 * Base widget class.
 */
dojo.declare('artifactory.BaseWidget', null, {
    constructor:function () {
        DojoUtils.monitor(this);

        this.init.apply(this, arguments);

        var me = this;
        var args = arguments;
        dojo.addOnLoad(function () {
            me.onLoad.apply(me, args);
        });
    }
});

dojo.declare("artifactory.dnd.Source", dojo.dnd.Source, {
    constructor:function () {
        // needed for preserveState()
        this.node.sourceWidget = this;
    },

    // Ignore mouse down event if clicked inside my node
    // (not in one of my items, for example, mousedown in scrollbars).
    onMouseDown:function (e) {
        // summary: event processor for onmousedown
        // e: Event: mouse event
        if (e.target != this.node) {
            artifactory.dnd.Source.superclass.onMouseDown.call(this, e);
        }
    },

    markupFactory:function (params, node) {
        params._skipStartup = true;
        return new artifactory.dnd.Source(node, params);
    },

    copyState:function () {
        return false;
    },

    updateIndices:function () {
        var selected = [];
        this.forEachSelected(function (item, i) {
            selected.push(i);
        });
        this.selectedIndices = selected;
    },

    onDndDrop:function (source, nodes, isCopy, target) {
        this.inherited('onDndDrop', arguments);
        this.selectable.onDrop(source, target);
    },

    destroy:function () {
        this.preserveState();
        delete this.node.sourceWidget;
        delete this.selectedIndices;

        this.inherited('destroy', arguments);
    },

    /**
     * Transfer state to newly created widget.
     */
    preserveState:function () {
        // find new widget
        var newDomNode = dojo.byId(this.node.id);
        if (!newDomNode) {
            return;
        }
        var newWidget = newDomNode.sourceWidget;
        if (!newWidget) {
            return;
        }

        // transfer selections
        var items = this.node.childNodes;
        var newItems = newWidget.node.childNodes;
        dojo.forEach(this.selectedIndices, function (index) {
            var newItem = newItems[index];
            if (newItem) {
                // re-select item
                newWidget._addItemClass(newItem, "Selected");
                newWidget.selection[newItem.id] = 1;
            }
        });

        newWidget.updateIndices();
        if (newWidget.getSelectedNodes().length) {
            newWidget.selectable.onSelection();
        }
    },

    forEach:function (func) {
        var item = this.node.firstChild;
        var i = 0;
        while (item) {
            func(item, i);
            i++;
            item = item.nextSibling;
        }
    },

    forEachSelected:function (func) {
        var item = this.node.firstChild;
        var i = 0;
        while (item) {
            var next = item.nextSibling;
            if (this.selection[item.id]) {
                func(item, i);
            }
            i++;
            item = next;
        }
    },

    forEachSelectedReverse:function (func) {
        var item = this.node.lastChild;
        while (item) {
            var prev = item.previousSibling;
            if (this.selection[item.id]) {
                func(item);
            }
            item = prev;
        }
    },

    isAnySelected:function () {
        var item = this.node.firstChild;
        while (item) {
            var next = item.nextSibling;
            if (this.selection[item.id] && item.className.indexOf('disabled') < 0) {
                return true;
            }
            item = next;
        }
        return false;
    }
});

/**
 * Selectable module.
 *
 * Selection is stored on parent domNode
 */
dojo.declare('artifactory.Selectable', artifactory.BaseWidget, {
    constructor:function () {
    },

    instantiate:function (sourceNode) {
        var me = this;
        DomUtils.addOnRender(function () {
            // instantiate widgets
            var widgets = dojo.parser.instantiate([sourceNode]);
            var source = widgets[0];
            source.autoSync = true;
            source.selectable = me;
        });
    },

    /**
     * Util.
     * Clears text elements and set onclick event.
     *
     * @param sourceNode list for nodes to init
     * @param initFunc optional. init item function
     */
    initSourceNode:function (sourceNode, initFunc) {
        if (!initFunc) {
            initFunc = DomUtils.cancel;
        }

        var me = this;
        this.instantiate(sourceNode);

        var next;
        var i = 0;
        var item = sourceNode.firstChild;
        while (item) {
            next = item.nextSibling;
            if (item.nodeType == 1) {
                item.onclick = function () {
                    sourceNode.sourceWidget.updateIndices(); // needed for preserveState()
                    me.onSelection(this);
                };

                initFunc(item, i);
                i++;
            } else {
                item.parentNode.removeChild(item);
            }
            item = next;
        }
    },

    /**
     * Util.
     * Sets button enabled/disabled.
     *
     * @param link link ref
     * @param cssClass base link css
     * @param enabled is link enabled
     */
    setButtonEnabled:function (link, cssClass, enabled) {
        if (enabled && link.disabled) {
            link.disabled = false;
            link.className = 'button ' + cssClass;
        } else if (!enabled && !link.disabled) {
            link.disabled = true;
            link.className += 'button ' + cssClass + '-disabled';
        }
    },

    /**
     * Event handler.
     */
    onSelection:function () {
    }
});
