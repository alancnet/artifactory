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

package org.artifactory.webapp.wicket.page.browse.home;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.request.cycle.RequestCycle;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.util.HttpUtils;

import javax.servlet.http.Cookie;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Yoav Aharoni
 */
public class RememberPageBehavior extends Behavior {
    static final String COOKIE_NAME = "art-page";
    private final boolean keepParameters;

    /**
     * Like {@link RememberPageBehavior#RememberPageBehavior(boolean)} called with "false"
     */
    public RememberPageBehavior() {
        this(false);
    }

    /**
     * @param keepParameters if true this behavior will save the whole URL including query parameters. Otherwise only
     *                       the base URL without the query parameters will be saved.
     */
    public RememberPageBehavior(boolean keepParameters) {
        this.keepParameters = keepParameters;
    }

    @Override
    public void beforeRender(Component component) {
        Page page = component.getPage();
        String pageUrl = getPageUrl(page);
        Cookie cookie = new Cookie(COOKIE_NAME, pageUrl);
        String contextPath = WicketUtils.getHttpServletRequest().getContextPath();
        cookie.setPath(contextPath);
        WicketUtils.getWebResponse().addCookie(cookie);
    }

    private String getPageUrl(Page page) {
        Class<? extends Page> pageClass = page.getClass();
        String pageUrl = RequestCycle.get().urlFor(pageClass, page.getPageParameters()).toString();
        if (!keepParameters) {
            pageUrl = HttpUtils.stripQuery(pageUrl);
        }
        try {
            return new URL(WicketUtils.toAbsolutePath(pageUrl)).getFile();
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
