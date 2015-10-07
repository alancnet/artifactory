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

function LinksColumn(iconId, panelId) {
    this.icon = document.getElementById(iconId);
    this.domNode = document.getElementById(panelId);
    this.parentNode = this.domNode.parentNode;
    this.hasFocus = false;
    this.icon.LinksColumn = this;
    this.domNode.LinksColumn = this;
    this.row = this.getRowAnchor();

    this.connentEvents();
}

LinksColumn.prototype.connentEvents = function () {
    var me = this;

    var hideIfNoFocus = function () {
        me.hasFocus = false;
        setTimeout(function () {
            if (!me.hasFocus) {
                me.hide();
            }
        }, 100);
    };

    // connent row mouse events
    dojo.connect(this.row, 'onmouseover', function () {
        me.hasFocus = true;
        setTimeout(function () {
            if (me.hasFocus) {
                me.show();
            }
        }, 100);
    });

    dojo.connect(this.row, 'onmouseout', hideIfNoFocus);

    // connenct panel mouse events
    dojo.connect(me.domNode, 'onmouseover', function () {
        me.hasFocus = true;
    });

    dojo.connect(me.domNode, 'onmouseout', hideIfNoFocus);

    var links = dojo.query('a', me.domNode);

    // fix RTFACT-2103
    if (dojo.isIE) {
        links.connect('onmouseover', function () {
            DomUtils.removeStyle(this, 'normal', true);
        });
        links.connect('onclick', function () {
            DomUtils.addStyle(this, 'normal');
        });
    }

    links.connect('onclick', function () {
        me.hide();
    });
};

LinksColumn.prototype.getRowAnchor = function () {
    var rowAnchor = this.icon;
    var tagName;
    do {
        rowAnchor = rowAnchor.parentNode;
        tagName = rowAnchor.tagName.toLowerCase();
    } while (tagName != 'tr' && tagName != 'li');

    return rowAnchor;
};

LinksColumn.prototype.show = function () {
    var parent = this.domNode.parentNode;
    var cord = dojo.coords(this.icon, true);
    if (parent == this.parentNode) {
        parent.removeChild(this.domNode);
        document.body.appendChild(this.domNode);
    }

    this.domNode.style.display = 'block';
    this.domNode.style.top = (cord.y - this.domNode.scrollHeight / 2 + 7) + 'px';
    this.domNode.style.left = (cord.x - this.domNode.scrollWidth + 5) + 'px';
    DomUtils.addStyle(this.row, 'opened');

    LinksColumn.current = this;

    // connect to onkeyup to catch ESC press
    if (!LinksColumn.connection) {
        LinksColumn.connection = dojo.connect(document, 'onkeyup', LinksColumn.onkeyup);
    }
};

LinksColumn.prototype.hide = function () {
    if (this.domNode.parentNode != this.parentNode) {
        this.domNode.parentNode.removeChild(this.domNode);
        this.parentNode.appendChild(this.domNode);
    }
    this.domNode.style.display = 'none';
    DomUtils.removeStyle(this.row, 'opened');

    dojo.disconnect(LinksColumn.connection);
    LinksColumn.current = null;
    LinksColumn.connection = null;
};

LinksColumn.hideCurrent = function () {
    if (LinksColumn.current) {
        try {
            LinksColumn.current.hide();
        } catch (e) {
        }
    }
};

LinksColumn.onkeyup = function (e) {
    var event = e ? e : window.event;
    if (event.keyCode == 27 && LinksColumn.current) {
        LinksColumn.current.hide();
    }
};