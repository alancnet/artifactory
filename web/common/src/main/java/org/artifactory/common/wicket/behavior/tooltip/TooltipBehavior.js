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

dojo.provide('artifactory.Tooltip');

dojo.require('dijit.Tooltip');
dojo.require('dojo.parser');

dojo.declare('artifactory.Tooltip', dijit.Tooltip, {
    postCreate:function () {
        if (!dijit._masterTT) {
            dijit._masterTT = new dijit._MasterTooltip();
        }
        dijit._masterTT.connect(dijit._masterTT.domNode, 'onmouseover', this.ttPersist);
        dijit._masterTT.connect(dijit._masterTT.domNode, 'onmouseout', this.ttFade);

        this.inherited('postCreate', arguments);
    },

    ttPersist:function (e) {
        this.fadeOut.stop();
        this.fadeIn.play();
    },

    ttFade:function (e) {
        this.fadeOut.play();
    }
});

artifactory.Tooltip.create = function (bubbleId, enabled) {
    var bubble = dijit.byId(bubbleId);
    if (bubble) {
        bubble.destroy();
    }
    if (enabled) {
        dojo.parser.instantiate([dojo.byId(bubbleId)]);
    }
};
