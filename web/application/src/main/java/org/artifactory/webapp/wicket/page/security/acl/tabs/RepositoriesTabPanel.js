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

(function () {
    var superResize = PermissionTabPanel.resize;
    PermissionTabPanel.resize = function () {
        var modalHeight = superResize();

        var modal = Wicket.Window.current;
        if (modal) {
            // resize repo list to fit modal
            var panel = dojo.byId('${repoListId}');
            var offset = dojo.coords(panel).t - 30;
            if (dojo.isIE) {
                offset = Math.max(240, offset);
            }

            var height = modalHeight - offset;
            height = Math.max(height, 120);

            dojo.query('#${repoListId} ul').forEach(function (ul) {
                ul.style.height = height + 'px';
            });
            var marginTop = Math.max(0, (height - 100) / 2);
            dojo.query('#${repoListId} .sep')[0].style.marginTop = marginTop + 'px';
        }
    };
})();