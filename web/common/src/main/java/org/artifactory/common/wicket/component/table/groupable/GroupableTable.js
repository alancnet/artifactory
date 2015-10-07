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

var GroupableTable = {
    collapseExpand:function (link) {
        var row = link.parentNode;
        var expanded = row.className.indexOf('group-expanded') >= 0;
        if (expanded) {
            row.className = 'group-header-row group-collapsed';
        } else {
            row.className = 'group-header-row group-expanded';
        }

        while (true) {
            row = DomUtils.nextSibling(row);
            if (!row || row.className.indexOf('group-header-row') >= 0) {
                break;
            }

            if (expanded) {
                DomUtils.addStyle(row, 'row-collapsed');
            } else {
                DomUtils.removeStyle(row, 'row-collapsed', true);
            }
        }
        return false;
    }
};