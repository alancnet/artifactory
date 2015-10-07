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

function FeedbackMessage(message, level) {
    this.message = message;
    this.level = level;
}

var AggregateFeedbackPanel = {
    onClear:function (panelId) {
        var panel = dojo.byId(panelId);
        dojo._setOpacity(panel, 0);
    },

    onShow:function (panelId) {
        var panel = dojo.byId(panelId);

        // count messages & get max width
        var count = {
            ERROR:0,
            WARNING:0,
            INFO:0
        };

        var width = 0;
        var messages = panel.getElementsByTagName('li');
        dojo.forEach(messages, function (li) {
            count[li.level]++;
            width = Math.max(width, li.firstChild.offsetWidth);
        });

        // add title
        var title = AggregateFeedbackPanel.getTitle(count);
        if (title) {
            // give same width to all messages
            dojo.forEach(messages, function (li) {
                li.firstChild.style.width = width + 'px';
            });

            // add title
            var div = document.createElement('div');
            div.className = 'feedback-title feedback-title-' + title.level;
            div.innerHTML = '<span>' + title.message + '</span>';
            var ul = panel.firstChild;
            ul.className += ' aggregate-feedback feedback-' + title.level;
            panel.insertBefore(div, ul);
        }

        // scroll to messages
        setTimeout(function () {
            DomUtils.scrollIntoView(panel);
        }, 100);

        // notify effect
        dojo.fadeIn({node:panel, duration:500}).play();
    },

    getTitle:function (count) {
        if (count.ERROR > 1) {
            return new FeedbackMessage(count.ERROR + " errors have been detected:", 'ERROR');
        }
        if (count.INFO > 1) {
            return new FeedbackMessage(count.INFO + " messages:", 'INFO');
        }
        if (count.WARNING > 1) {
            return new FeedbackMessage("Please notice the following warnings:", 'WARNING');
        }
        return null;
    }
};