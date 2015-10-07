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

var SubMenuPanel = {
    OPENED_CSS:'sub-menu-opened',
    CLOSED_CSS:'sub-menu-closed',

    toogleMenu:function (cookieName, link) {
        var menuItem = DomUtils.findParent(link, 'li')
        var menuGroup = DomUtils.nextSibling(menuItem);
        var isOpened = menuGroup.className == SubMenuPanel.OPENED_CSS;

        if (isOpened) {
            // close menu
            CookieUtils.setCookie(cookieName, 'false');
            menuItem.className = menuItem.className.replace(/menu-group-opened/, 'menu-group-enabled');
            menuGroup.className = SubMenuPanel.CLOSED_CSS;
        } else {
            // open menu
            CookieUtils.setCookie(cookieName, 'true');
            menuItem.className = menuItem.className.replace(/menu-group-enabled/, 'menu-group-opened');
            menuGroup.className = SubMenuPanel.OPENED_CSS;
        }
        SubMenuPanel.onToggle();
        return false;
    },

    onToggle:DomUtils.cancel
};


var CookieUtils = {
    setCookie:function (name, value, days) {
        var expires;
        if (days) {
            var date = new Date();
            date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
            expires = '; expires=' + date.toGMTString();
        } else {
            expires = '';
        }
        document.cookie = name + '=' + escape(value) + expires + '; path=/';
    },

    clearCookie:function (name) {
        CookieUtils.setCookie(name, '', -1);
    }
};

// RTFACT-3689
if (dojo.isIE >= 7) {
    DomUtils.addOnLoad(function () {
        var sideMenu = dojo.byId('sideMenu');

        function fixSidebar() {
            sideMenu.parentNode.style.minHeight = sideMenu.scrollHeight + 'px';
        }

        sideMenu.style.position = 'absolute';
        fixSidebar();

        SubMenuPanel.onToggle = fixSidebar;
    });
}