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

package org.artifactory.common.wicket.util;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;

import javax.servlet.http.Cookie;
import java.util.List;

/**
 * @author Yoav Aharoni
 */
public abstract class CookieUtils {
    private CookieUtils() {
        // utility class
    }

    public static String getCookie(String name) {
        WebRequest request = (WebRequest) RequestCycle.get().getRequest();
        Cookie cookie = request.getCookie(name);
        if (cookie == null) {
            return null;
        }
        return cookie.getValue();
    }

    public static void setCookie(String name, Object value) {
        if (value == null) {
            clearCookie(name);
            return;
        }

        WebResponse response = (WebResponse) RequestCycle.get().getResponse();
        response.addCookie(new Cookie(name, value.toString()));
    }

    public static void clearCookie(String name) {
        WebResponse response = (WebResponse) RequestCycle.get().getResponse();
        response.clearCookie(new Cookie(name, null));
    }

    /**
     * Search for a persistent component cookie. for every web page there should be only one cookie for every
     * component
     *
     * @param componentId
     * @return The first cookie if exist, that his name ends with the component id
     */
    public static Cookie getCookieByComponentId(String componentId) {
        WebRequest request = (WebRequest) RequestCycle.get().getRequest();
        List<Cookie> cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie c : cookies) {
            if (c.getName().endsWith(componentId)) {
                return c;
            }
        }
        return null;
    }
}
