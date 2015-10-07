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

package org.artifactory.webapp.wicket.application;

import org.apache.wicket.request.cycle.RequestCycle;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Logout handler for integration with Spring security.
 *
 * @author Noam Y. Tenne
 */
@Component
public class WicketLogoutHandler implements LogoutHandler {

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (RequestCycle.get() != null) {
            // we are in a wicket request cycle
            ArtifactoryWebSession.get().signOutArtifactory();
        } else {
            /**
             * Outside of a Wicket request cycle invalidate the existing {@link HttpSession}. Most chances this will
             * happen when the access filter detects that a re-authentication must be performed.
             */
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        }
    }
}
