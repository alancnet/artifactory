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

package org.artifactory.webapp.wicket.page.home;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.SamlAddon;
import org.artifactory.addon.wicket.WebApplicationAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.links.TitledPageLink;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.util.NumberFormatter;
import org.artifactory.util.SerializablePair;
import org.artifactory.webapp.wicket.application.ArtifactoryWebSession;
import org.artifactory.webapp.wicket.page.base.EditProfileLink;
import org.artifactory.webapp.wicket.page.base.LoginLink;
import org.artifactory.webapp.wicket.page.browse.treebrowser.BrowseRepoPage;
import org.artifactory.webapp.wicket.page.config.advanced.storage.StorageSummaryPage;
import org.artifactory.webapp.wicket.page.home.news.ArtifactoryUpdatesPanel;
import org.ocpsoft.prettytime.PrettyTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * @author Yoav Aharoni
 */
public class WelcomeBorder extends TitledBorder {
    private static final Logger log = LoggerFactory.getLogger(WelcomeBorder.class);

    @SpringBean
    private AddonsManager addonsManager;

    @SpringBean
    private AuthorizationService authorizationService;

    @SpringBean
    private CentralConfigService centralConfigService;

    @SpringBean
    private RepositoryService repoService;

    @SpringBean
    private SecurityService securityService;

    private Map<String, String> headersMap = WicketUtils.getHeadersMap();

    public WelcomeBorder(String id) {
        super(id);
        addUptime();
        addArtifactsCount();
        addCurrentUserInfo();
        addVersionInfo();

        add(new ArtifactoryUpdatesPanel("news"));
    }

    private void addUptime() {
        WebApplicationAddon applicationAddon = addonsManager.addonByType(WebApplicationAddon.class);
        Label uptimeLabel = applicationAddon.getUptimeLabel("uptime");
        add(uptimeLabel);
    }

    private void addArtifactsCount() {
        Component countLabel;
        if (authorizationService.isAdmin()) {
            countLabel = new TitledPageLink("artifactsCount", "", StorageSummaryPage.class);
        } else {
            countLabel = new Label("artifactsCount", "");
        }
        add(countLabel);
        try {
            long count = repoService.getArtifactCount();
            countLabel.setDefaultModelObject(NumberFormatter.formatLong(count));
        } catch (RepositoryRuntimeException e) {
            countLabel.setVisible(false);
            log.warn("Failed to retrieve artifacts count: " + e.getMessage());
        }
    }

    private void addCurrentUserInfo() {
        add(new TitledPageLink("browseLink", "browse", BrowseRepoPage.class));
        add(new LoginLink("loginLink", "log in"));
        SamlAddon samlAddon = addonsManager.addonByType(SamlAddon.class);
        AbstractLink logoutLink = samlAddon.getLogoutLink("logoutLink");
        add(logoutLink);
        addLastLoginLabel();
        //addLastAccessLabel();
        add(new EditProfileLink("profileLink"));
    }

    private void addLastLoginLabel() {
        SerializablePair<String, Long> lastLoginInfo = null;
        boolean authenticated = authorizationService.isAuthenticated();
        boolean anonymousLoggedIn = authorizationService.isAnonymous();

        //If a user (not anonymous) is logged in
        if (authenticated && !anonymousLoggedIn) {
            String username = authorizationService.currentUsername();
            if (!StringUtils.isEmpty(username)) {
                //Try to get last login info
                lastLoginInfo = ArtifactoryWebSession.get().getLastLoginInfo();
            }
        }
        final boolean loginInfoValid = lastLoginInfo != null && lastLoginInfo.isNotNull();

        Label lastLogin = new Label("lastLogin", new Model());
        lastLogin.setVisible(loginInfoValid);
        add(lastLogin);
        if (loginInfoValid) {
            Date date = new Date(lastLoginInfo.getSecond());
            String clientIp = lastLoginInfo.getFirst();
            PrettyTime prettyTime = new PrettyTime();
            lastLogin.setDefaultModelObject("Last logged in: " + prettyTime.format(
                    date) + " (" + date.toString() + "), from " + clientIp + ".");
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void addLastAccessLabel() {
        SerializablePair<String, Long> lastAccessInfo = null;
        boolean authenticated = authorizationService.isAuthenticated();
        boolean anonymousLoggedIn = authorizationService.isAnonymous();

        //If a user (not anonymous) is logged in
        if (authenticated && !anonymousLoggedIn) {
            String username = authorizationService.currentUsername();
            if (!StringUtils.isEmpty(username)) {
                //Try to get last login info
                lastAccessInfo = securityService.getUserLastAccessInfo(username);
            }
        }
        final boolean lastAccessValid = (lastAccessInfo != null);

        Label lastAccess = new Label("lastAccess", new Model()) {
            @Override
            public boolean isVisible() {
                return lastAccessValid;
            }
        };
        add(lastAccess);
        if (lastAccessValid) {
            Date date = new Date(lastAccessInfo.getSecond());
            String clientIp = lastAccessInfo.getFirst();
            PrettyTime prettyTime = new PrettyTime();
            lastAccess.setDefaultModelObject(
                    "Last access in: " + prettyTime.format(date) + " (" + date.toString() + "), from "
                            + clientIp + ".");
        }
    }

    private void addVersionInfo() {
        WebApplicationAddon applicationAddon = addonsManager.addonByType(WebApplicationAddon.class);
        applicationAddon.addVersionInfo(this, headersMap);
    }
}