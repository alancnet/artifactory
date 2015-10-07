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

dojo.declare('artifactory.SortedDragDropSelection', artifactory.DragDropSelection, {
    init:function () {
        this.inherited('init', arguments);
        this.sort();
    },

    onDrop:function () {
        this.sort();
        this.inherited(arguments);
    },

    sort:function () {
        this.sortList(this.sourceNode);
        this.sortList(this.targetNode);
    },

    moveSelectedItems:function (fromList, toList) {
        this.inherited(arguments);
        this.sortList(toList);
    },

    sortList:function (ul) {
        var liList = ul.childNodes;
        var liArray = this.nodeListToArray(liList);
        liArray = liArray.sort(function (o1, o2) {
            var s1 = o1.innerHTML.toLowerCase();
            var s2 = o2.innerHTML.toLowerCase();
            return ((s1 < s2) ? -1 : ((s1 > s2) ? 1 : 0));
        });

        dojo.forEach(liArray, function (li) {
            ul.appendChild(li);
        });
    },

    nodeListToArray:function (collection) {
        var ary = [];
        for (var i = 0, len = collection.length; i < len; i++) {
            ary.push(collection[i]);
        }
        return ary;
    }
});

dojo.declare('artifactory.DisabledSortedDragDropSelection', artifactory.SortedDragDropSelection, {
    instantiate:DomUtils.cancel
});
