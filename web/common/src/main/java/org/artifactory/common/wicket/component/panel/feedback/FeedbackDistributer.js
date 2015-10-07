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

/**
 * Distributes feedback messages to FeedbackMessagesPanel.
 *
 * @author Yoav Aharoni
 */
if (!window.FeedbackDistributer) {
    FeedbackDistributer = {
        defaultPanel:null,
        panels:new Array(),

        init:function (defaultPanel) {
            FeedbackDistributer.defaultPanel = dojo.byId(defaultPanel);
            FeedbackDistributer.clearMessages();
        },

        clearMessages:function () {
            if (FeedbackDistributer.panels.length == 0) {
                return;
            }
            dojo.forEach(FeedbackDistributer.panels, function (panel) {
                try {
                    while (panel.firstChild) {
                        var ondestroy = panel.getAttribute('ondestroy');
                        if (ondestroy) {
                            eval(ondestroy);
                        }
                        panel.removeChild(panel.firstChild);
                    }
                } catch (e) {
                }
            });

            FeedbackDistributer.panels = new Array();
        },

        showMessages:function () {
            dojo.forEach(FeedbackDistributer.panels, function (panel) {
                if (panel.getElementsByTagName('li').length) {
                    // trigger panel.onShow() event handler
                    var onshow = panel.getAttribute('onshow');
                    if (onshow) {
                        eval(onshow);
                    }
                }
            });
        },

        addMessage:function (reporterId, level, message) {
            // get reporter and feedback panel
            var reporter = dojo.byId(reporterId);
            var panel = FeedbackDistributer.getFeedbackPanelFor(reporter);

            // setup panel if needed
            if (!panel.hasChildNodes()) {
                var ul = document.createElement('ul');
                ul.className = 'feedback';
                panel.appendChild(ul);
                FeedbackDistributer.panels.push(panel);

                // trigger panel.onClear() event handler
                var onclear = panel.getAttribute('onclear');
                if (onclear) {
                    eval(onclear);
                }
            }

            // add message to panel
            var li = document.createElement('li');
            li.className = 'feedbackPanel' + level;
            li.innerHTML = '<span>' + message + '</span>';
            li.reporter = reporter;
            li.level = level;
            panel.firstChild.appendChild(li);
        },

        getFeedbackPanelFor:function (reporter) {
            var node = reporter;

            // find closest FeedbackPanel
            while (node && node != document.body) {
                var feedbackId = node.getAttribute('feedbackId');
                if (feedbackId) {
                    return dojo.byId(feedbackId);
                }

                node = node.parentNode;
            }

            // add defaultPanel if needed
            if (!FeedbackDistributer.defaultPanel) {
                var dp = document.createElement('div');
                document.body.appendChild(dp);
                FeedbackDistributer.defaultPanel = dp;
            }

            // return defaultPanel
            return FeedbackDistributer.defaultPanel;
        }
    };
}