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

package org.artifactory.webapp.wicket.page.security.login;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.security.AuthenticationHelper;
import org.artifactory.webapp.wicket.application.ArtifactoryApplication;
import org.artifactory.webapp.wicket.page.base.BasePage;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Yoav Landman
 */
public class LogoutPage extends BasePage {

    public LogoutPage() {
        TitledBorder border = new TitledBorder("logoutBorder", "outer-border");
        add(border);
        border.add(new LogoutPanel("logoutPanel"));
    }

    @Override
    protected void init() {
        HttpServletRequest servletRequest = WicketUtils.getHttpServletRequest();
        HttpServletResponse servletResponse = WicketUtils.getHttpServletResponse();
        Authentication authentication = AuthenticationHelper.getAuthentication();

        Map<String, LogoutHandler> logoutHandlers = ContextHelper.get().beansForType(LogoutHandler.class);
        for (LogoutHandler logoutHandler : logoutHandlers.values()) {
            logoutHandler.logout(servletRequest, servletResponse, authentication);
        }
        super.init();
    }

    @Override
    public String getPageName() {
        return "Sign out";
    }

    @Override
    protected Class<? extends BasePage> getMenuPageClass() {
        return ArtifactoryApplication.get().getHomePage();
    }
}
