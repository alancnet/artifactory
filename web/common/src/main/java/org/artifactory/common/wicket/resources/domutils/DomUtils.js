/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2015 JFrog Ltd.
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

/*-- Utils --*/

function cancel(e) {
    window.lastTarget = e.srcElement;
    window.lastEvent = e;
    e.cancelBubble = true;
    e.returnValue = false;
    if (e.preventDefault) {
        e.preventDefault();
    }
    if (e.stopPropagation) {
        e.stopPropagation();
    }
    return false;
}

/*-- DOM Utils --*/

var DomUtils = {
    footerHeight: 0,

    addHoverStyle: function(obj) {
        DomUtils.addStyle(obj, 'hover');
    },

    submitOnce: function (form) {
        var disableButton = function(button) {
            if (button.type == 'submit') {
                button.disabled = true;
            }
        };
        dojo.forEach(form.getElementsByTagName('button'), disableButton);
        dojo.forEach(form.getElementsByTagName('input'), disableButton);
    },

    removeHoverStyle: function(obj) {
        DomUtils.removeStyle(obj, 'hover');
    },

    addStyle: function(obj, append) {
        if (!obj) {
            return;
        }

        // 'register' style
        if (!obj.addedStyles) {
            obj.addedStyles = {};
        }

        if (obj.addedStyles[append]) {
            return;
        }
        obj.addedStyles[append] = true;


        // add style to className
        var trimmed = obj.className.replace(/^\s+|\s+$/g, '');
        obj.className = trimmed + ' ' + append + ' ' + trimmed.replace(/\s/g, '-' + append + ' ') + '-' + append;
    },

    removeStyle: function(obj, append, force) {
        if (!obj) {
            return;
        }

        // check if style registered
        if (!force && (!obj.addedStyles || !obj.addedStyles[append])) {
            return;
        }

        if (obj.addedStyles) {
            obj.addedStyles[append] = undefined;
        }

        // remove style from className & cleanup className
        var classes = obj.className.split(' ');
        var regExp = new RegExp(append + '$');
        var map = {};
        dojo.forEach(classes, function(className) {
            if (className && !className.match(regExp)) {
                map[className] = true;
            }
        });

        var cssClass = '';
        for (var className in map) {
            if (className) {
                cssClass += ' ' + className;
            }
        }
        obj.className = cssClass.substring(1);
    },

    findParent: function(node, tagName) {
        tagName = tagName.toLowerCase();

        var current = node;
        while (current && current.tagName && current.tagName.toLowerCase() != tagName) {
            current = current.parentNode;
        }
        return current;
    },

    nextSibling: function(node) {
        do {
            node = node.nextSibling;
        } while (node && node.nodeType != 1);
        return node;
    },

    addOnRender: function(func) {
        if (dojo.isIE) {
            Wicket.Event.addDomReadyEvent(func);
        } else {
            func();
        }
    },

    addOnLoad: function(func) {
        // needed becuase dojo.addOnLoad executed later for IE
        if (!dojo._postLoad) {
            dojo.addOnLoad(func);
        } else {
            func();
        }
    },

    cancel: function() {
        return false;
    },

    scrollIntoView: function(node) {
        var body = document.body;
        var current = node;
        var parent = current.parentNode;
        while (parent && parent != body) {
            var overflow = dojo.style(parent).overflow;
            if (overflow == 'auto' || overflow == 'scroll') {
                DomUtils.scrollIntoParentView(current, parent);
                current = parent;
            }
            parent = parent.parentNode;
        }

        DomUtils.scrollIntoBody(node);
    },

    scrollIntoParentView: function(node, parent, parentCoords) {
        var coords = dojo.coords(node, true);
        if (!parentCoords) {
            parentCoords = dojo.coords(parent, true);
        }

        var top = coords.y + coords.h;
        var parentTop = parentCoords.y + parentCoords.h;
        var center = parentCoords.h / 2 - coords.h / 2;

        if (top > parentTop) {
            parent.scrollTop += top - parentTop + center;
        } else if (coords.y < parentCoords.y) {
            parent.scrollTop -= parentCoords.y - coords.y + center;
        }
    },

    scrollIntoBody: function(node) {
        var parent = dojo.isWebKit ? document.body : document.documentElement;
        var parentCoords = {
            y: parent.scrollTop,
            h: parent.clientHeight
        };
        DomUtils.scrollIntoParentView(node, parent, parentCoords);
    },

    bindHeight: function(node, getHeight, bind) {
        var docHeight = document.documentElement.offsetHeight - DomUtils.footerHeight;
        node.style.height = getHeight(docHeight) + 'px';

        if (bind) {
            dojo.connect(window, 'onresize', function() {
                var docHeight = document.documentElement.offsetHeight;
                node.style.height = getHeight(docHeight) + 'px';
            });
        }
    },

    autoHeight: function(node, container) {
        var height = node.clientHeight - container.scrollHeight + container.clientHeight - 5;
        node.style.height = height + 'px';
    }
};

/*-- Dojo Stuff ---*/

var DojoUtils = {
    widgets: new Array(),

    // widget garbage collection
    dijitCleanup: function() {
        var widgets = new Array();
        var widget;
        while (widget = DojoUtils.widgets.pop()) {
            var node = widget.domNode || widget.node;
            var body = DomUtils.findParent(node, 'body');
            if (!body) {
                try {
                    // store replacing widget
                    var wgtById = dijit.byId(widget.id);

                    // destroy old widget
                    widget.destroy();

                    // if replaced, re-register new widget 
                    if (wgtById && wgtById != widget) {
                        dijit.registry.add(wgtById);
                    }
                } catch(e) {
                }
            } else {
                widgets.push(widget);
            }
        }
        DojoUtils.widgets = widgets;
    },

    monitor: function(widget) {
        DojoUtils.widgets.push(widget);
    },

    instantiate: function(id, onInit) {
        // destroy old widget
        var widget = dijit.byId(id);
        if (widget) {
            widget.destroy();
        }

        // create new widget
        var widgets = dojo.parser.instantiate([dojo.byId(id)]);
        if (onInit) {
            dojo.forEach(widgets, onInit);
        }
        return widgets;
    }
};

/*-- Init --*/

(function() {

    /*-- override 'dojo.parser.instantiate()' --*/

    dojo.require('dojo.parser');

    var superInstantiate = dojo.parser.instantiate;
    dojo.parser.instantiate = function() {
        var widgets = superInstantiate.apply(this, arguments);
        dojo.forEach(widgets, function(widget) {
            if (!widget.selfCleanup) {
                DojoUtils.monitor(widget);
            }
        });
        return widgets;
    };

    /*-- fix dojo._getIeDispatcher to return value --*/

    if (dojo._getIeDispatcher) {
        dojo._getIeDispatcher = function() {
            // yoava: added "return " +
            return new Function("return " + dojo._scopeName + "._ieDispatcher(arguments, this)");
        };
    }

    /*-- fix dijit.findWidgets() for IE --*/

    dijit.findWidgets = function(/*DomNode*/ root) {
        var outAry = [];

        function getChildrenHelper(root) {
            var list = dojo.isIE ? root.children : root.childNodes, i = 0, node;
            while (node = list[i++]) {
                if (node.nodeType != 1) {
                    continue;
                }
                var widgetId = node.getAttribute("widgetId");
                if (widgetId) {
                    var widget = dijit.byId(widgetId);
                    // yoava: added null-check
                    if (widget) {
                        outAry.push(widget);
                    }
                } else {
                    getChildrenHelper(node);
                }
            }
        }

        getChildrenHelper(root);
        return outAry;
    };

    /*-- detect browser for css --*/

    var css = '';
    var browsers = ['Opera','Khtml','WebKit','Chrome','Safari','FF','Moz','IE'];
    dojo.forEach(browsers, function(name) {
        var version = dojo['is' + name];
        if (version) {
            css += ' ' + name + ' ' + name + '-' + version;
        }
    });
    switch (dojo.isIE) {
        case 6:
            css += ' IE-67 IE-678';
            break;
        case 7:
            css += ' IE-67 IE-678 IE-78';
            break;
        case 8:
            css += ' IE-78 IE-678 IE-789 IE-89';
            break;
        case 9:
            css += ' IE-89 IE-789';
            break;
    }

    document.documentElement.className = navigator.platform + css;
})();
