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

var DisabledAddon = {
    create: function (id, position, iconClassName, addon, serverToken) {
        // create only once
        var node = dojo.byId(id);
        if (node.DisabledAddon) {
            return;
        }
        node.DisabledAddon = true;

        // add tooltip
        var widgets = DojoUtils.instantiate(id + '_bubble');
        if (position) {
            widgets[0].position = position;
        }

        // add icon
        var icon = document.createElement('span');
        icon.className = iconClassName;
        node.insertBefore(icon, node.firstChild);

        // Set link css class
        this.setChecked(dojo.byId(id + '_hide'), addon, serverToken);
    },

    toogle: function (link, id, serverToken, addon) {
        // toggle show/hide
        if (!link.className.match(/checked/)) {
            dojo.cookie('addon-' + addon, serverToken, {expires: 3650, path: artApp});
        } else {
            dojo.cookie('addon-' + addon, null, {expires: -1, path: artApp});
        }
        this.setChecked(link, addon, serverToken);

        // sync className
        dojo.byId(id + '_hide').className = link.className;
        return false;
    },

    setChecked: function (link, addon, serverToken) {
        // set show/hide of link
        var cookie = dojo.cookie('addon-' + addon);
        if (cookie == serverToken) {
            link.className = 'hide-link hide-link-checked';
        } else {
            link.className = 'hide-link';
        }
    }
};
