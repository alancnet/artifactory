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

PermissionTabPanel = {
    resize:function () {
        var modal = Wicket.Window.current;
        if (modal) {
            // resize tab to fit modal
            var panel = dojo.byId('${component.markupId}');
            var height = modal.content.clientHeight - 190;
            panel.style.height = height + 'px';
            return height;
        }
    },

    onShow:function () {
        PermissionTabPanel.resize();

        // set resize event handler
        var modal = Wicket.Window.current;
        if (modal) {
            modal.resizing = function () {
                Wicket.Window.prototype.resizing.apply(this, arguments);
                PermissionTabPanel.resize();
            };
        }
    }
};