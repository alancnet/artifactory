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

var StyledTabbedPanel = {
    init:function (tabsContainerId, moveLeftId, moveRightId) {
        var tabsContainer = dojo.byId(tabsContainerId);
        var moveLeft = dojo.byId(moveLeftId);
        var moveRight = dojo.byId(moveRightId);
        var tabs = tabsContainer.getElementsByTagName('li');
        var height = 100;

        tabsContainer.scrollTop = document._tabScrollTop;

        function move(dir) {
            var scrollTop = tabsContainer.scrollTop + height * dir;
            if (scrollTop >= 0 && scrollTop <= tabsContainer.scrollHeight - height) {
                tabsContainer.scrollTop = scrollTop;
                document._tabScrollTop = scrollTop;
            }
            initLinks();
            return false;
        }

        function initLinks() {
            // move buttons
            moveLeft.style.visibility = (tabsContainer.scrollTop - height >= 0) ? 'visible' : 'hidden';
            moveRight.style.visibility =
                    (tabsContainer.scrollTop + height <= tabsContainer.scrollHeight - height) ? 'visible' : 'hidden';

            fixScrollAlignment();
        }

        function fixScrollAlignment() {
            if (tabsContainer.scrollTop > tabsContainer.scrollHeight - height) {
                tabsContainer.scrollTop -= height;
            }

            var mod = tabsContainer.scrollTop % height;
            if (mod != 0) {
                tabsContainer.scrollTop -= mod;
            }
        }

        function initTabs() {
            var prevTab;
            dojo.forEach(tabs, function (tab) {
                DomUtils.removeStyle(tab, 'first-tab');
                DomUtils.removeStyle(tab, 'last-tab');

                if (tab.style.display != 'none') {
                    if (prevTab && tab.offsetTop != prevTab.offsetTop) {
                        DomUtils.addStyle(tab, 'first-tab');
                        DomUtils.addStyle(prevTab, 'last-tab');
                    }
                    prevTab = tab;
                }
            });

            DomUtils.addStyle(tabs[0], 'first-tab');
            DomUtils.addStyle(prevTab, 'last-tab');
        }

        function onResize() {
            initLinks();
            initTabs();
        }

        moveLeft.onclick = function () {
            return move(-1);
        };

        moveRight.onclick = function () {
            return move(1);
        };

        tabsContainer.resizeTabs = onResize;

        // connect to events
        dojo.disconnect(StyledTabbedPanel.onresize);
        StyledTabbedPanel.onresize = dojo.connect(window, 'onresize', onResize);

        dojo.addOnLoad(function () {
            setTimeout(onResize, 100);
        });

        // init
        onResize();
    }
};