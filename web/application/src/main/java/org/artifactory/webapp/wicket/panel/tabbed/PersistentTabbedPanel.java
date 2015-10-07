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

package org.artifactory.webapp.wicket.panel.tabbed;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.artifactory.common.wicket.util.CookieUtils;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.webapp.wicket.panel.tabbed.tab.DisabledTab;

import java.util.List;

/**
 * A StyledTabbedPanel that remembers the last selected tab.
 *
 * @author Yossi Shaul
 */
public class PersistentTabbedPanel extends StyledTabbedPanel {
    private static final String COOKIE_NAME = "browse-last-tab";
    private static final int UNSET = -1;

    private int lastTabIndex;
    public static final String SELECT_TAB_PARAM = "selectTab";

    public PersistentTabbedPanel(String id, List<ITab> tabs) {
        super(id, tabs);

        lastTabIndex = getLastTabIndex();
    }

    @Override
    protected void onBeforeRender() {
        selectLastTab();
        super.onBeforeRender();
    }

    /**
     * Re-select last selected tab. To be called before render becuase some tabs are loaded lazily (calling
     * selectLastTab from c'tor might cause NPE).
     */
    private void selectLastTab() {
        if (lastTabIndex != UNSET) {
            setSelectedTab(lastTabIndex);
            lastTabIndex = UNSET;
        }
    }

    /**
     * Return last tab index as stored in cookie.
     */
    @SuppressWarnings({"unchecked"})
    private int getLastTabIndex() {
        List<? extends ITab> tabs = getTabs();

        /**
         * If a parameter of a tab title to select was included in the request, give it priority over the
         * last-selected-tab cookie
         */
        String defaultSelectionTabTitle = getDefaultSelectionTabTitle();
        int indexToReturn = getTabIndexByTitle(defaultSelectionTabTitle, tabs);
        if (indexToReturn != -1) {
            CookieUtils.setCookie(COOKIE_NAME, defaultSelectionTabTitle);
            return indexToReturn;
        }

        indexToReturn = getTabIndexByTitle(CookieUtils.getCookie(COOKIE_NAME), tabs);
        if (indexToReturn != -1) {
            return indexToReturn;
        }

        return UNSET;
    }

    @Override
    protected void onAjaxUpdate(AjaxRequestTarget target) {
        super.onAjaxUpdate(target);

        // store last tab name in a cookie
        ITab tab = getTabs().get(getSelectedTab());
        CookieUtils.setCookie(COOKIE_NAME, tab.getTitle().getObject());
    }

    /**
     * Retrieves the default selection tab title from the web request.
     *
     * @return Default selection tab title. May be null if not included in request
     */
    private String getDefaultSelectionTabTitle() {
        return WicketUtils.getParameter(SELECT_TAB_PARAM);
    }

    private int getTabIndexByTitle(String tabTitle, List<? extends ITab> tabs) {
        if (StringUtils.isNotBlank(tabTitle)) {
            for (int i = 0; i < tabs.size(); i++) {
                ITab tab = tabs.get(i);
                String tabName = tab.getTitle().getObject();
                if (tabName.equals(tabTitle) && !(tab instanceof DisabledTab)) {
                    return i;
                }
            }
        }

        return UNSET;
    }
}
