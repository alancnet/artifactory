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

ArtifactoryUpdates = {
    HIDE_COOKIE:'new-h',
    READ_COOKIE:'new-r',

    read:function (me, cookie) {
        var container = me.parentNode.parentNode;
        if (container.className != 'news-open') {
            container.className = 'news-open';
            dojo.cookie(ArtifactoryUpdates.READ_COOKIE, cookie, {expires:3650});
        } else {
            container.className = 'news-close';
        }
        this.setChecked(dojo.byId('show'));
        return false;
    },

    toogle:function (me, cookie) {
        // toggle show/hide
        if (!me.className.match(/checked/)) {
            dojo.cookie(ArtifactoryUpdates.HIDE_COOKIE, cookie, {expires:3650});
        } else {
            dojo.cookie(ArtifactoryUpdates.HIDE_COOKIE, null, {expires:-1});
        }
        this.setChecked(me);
        return false;
    },
    setChecked:function (me) {
        // set show/hide of link
        if (dojo.cookie(ArtifactoryUpdates.HIDE_COOKIE) != null) {
            me.className = 'hide-link hide-link-checked';
        } else {
            me.className = 'hide-link';
        }
    },
    fadeIn:function (id) {
        var node = dojo.byId(id);
        dojo._setOpacity(node, 0);
        dojo.animateProperty({
            node:node,
            duration:1000,
            properties:{
                opacity:{ start:0.3, end:1 }
            }
        }).play();
    }
};