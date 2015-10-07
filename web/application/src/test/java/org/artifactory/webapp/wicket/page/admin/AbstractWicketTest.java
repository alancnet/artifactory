/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.webapp.wicket.page.admin;

import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.AddonsWebManager;
import org.artifactory.addon.CoreAddonsImpl;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.addon.wicket.*;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.config.VersionInfo;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;
import org.artifactory.security.SystemAuthenticationToken;
import org.artifactory.storage.StorageService;
import org.artifactory.storage.db.servers.service.ArtifactoryServersCommonService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.webapp.wicket.application.ArtifactoryApplication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.testng.annotations.BeforeClass;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Shay Yaakov
 */
public abstract class AbstractWicketTest {

    private ApplicationContextMock applicationContextMock;
    private ArtifactoryHomeBoundTest artifactoryHomeBoundTest;
    private ArtifactoryContext dummyArtifactoryContext;
    protected CentralConfigService configService;

    private WicketTester tester;

    @BeforeClass
    public void setUp() throws Exception {
        //Creates a new application context mock.
        applicationContextMock = new ApplicationContextMock();

        artifactoryHomeBoundTest = new ArtifactoryHomeBoundTest();
        artifactoryHomeBoundTest.bindArtifactoryHome();

        dummyArtifactoryContext = new DummyArtifactoryContext(applicationContextMock);
        ArtifactoryContextThreadBinder.bind(dummyArtifactoryContext);

        configService = mock(CentralConfigService.class);
        when(configService.getVersionInfo()).thenReturn(new VersionInfo(ConstantValues.artifactoryVersion.getString(),
                ConstantValues.artifactoryRevision.getString()));
        CentralConfigDescriptorImpl configDescriptor = new CentralConfigDescriptorImpl();
        configDescriptor.setFooter("bla");
        when(configService.getDescriptor()).thenReturn(configDescriptor);
        addMock("centralConfig", configService);

        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        //For authentication, use mock like this:
        //Authentication authentication = mock(Authentication.class);
        //when(authentication.isAuthenticated()).thenReturn(true);
        //when(authentication.getPrincipal()).thenReturn("admin");
        //when(authentication.getAuthorities()).thenReturn(SimpleUser.ADMIN_GAS);

        //or real authentication like this:
        //((ArtifactoryWebSession) getTester().getSession()).signIn("admin", "password");
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(new SystemAuthenticationToken());
        addMock("authenticationManager", authenticationManager);

        AddonsManager addonsManager = mock(AddonsManager.class);
        when(addonsManager.isInstantiationAuthorized((Class) notNull())).thenReturn(true);
        when(addonsManager.addonByType(WebApplicationAddon.class)).thenReturn(new WicketAddonsImpl());
        when(addonsManager.addonByType(SearchAddon.class)).thenReturn(new WicketAddonsImpl());
        when(addonsManager.addonByType(PropertiesWebAddon.class)).thenReturn(new WicketAddonsImpl());
        when(addonsManager.addonByType(LicensesWebAddon.class)).thenReturn(new WicketAddonsImpl());
        when(addonsManager.addonByType(BlackDuckWebAddon.class)).thenReturn(new WicketAddonsImpl());
        when(addonsManager.addonByType(HaWebAddon.class)).thenReturn(new WicketAddonsImpl());
        when(addonsManager.addonByType(CrowdWebAddon.class)).thenReturn(new WicketAddonsImpl());
        when(addonsManager.addonByType(SamlWebAddon.class)).thenReturn(new WicketAddonsImpl());
        when(addonsManager.addonByType(HttpSsoAddon.class)).thenReturn(new WicketAddonsImpl());
        when(addonsManager.addonByType(WebstartWebAddon.class)).thenReturn(new WicketAddonsImpl());
        when(addonsManager.addonByType(SamlAddon.class)).thenReturn(new WicketAddonsImpl());
        when(addonsManager.isLicenseInstalled()).thenReturn(true);
        when(addonsManager.addonByType(HaCommonAddon.class)).thenReturn(new CoreAddonsImpl());
        addMock("addonsManager", addonsManager);

        AuthorizationService authorizationService = mock(AuthorizationService.class);
        when(authorizationService.isAnonymous()).thenReturn(false);
        when(authorizationService.isAdmin()).thenReturn(true);
        addMock("authorizationService", authorizationService);

        AddonsWebManager addonsWebManager = mock(AddonsWebManager.class);
        addMock("AddonsWebManager", addonsWebManager);

        ArtifactoryServersCommonService serversCommonService = mock(ArtifactoryServersCommonService.class);
        addMock("serversService", serversCommonService);

        SecurityService securityService = mock(SecurityService.class);
        when(securityService.getUserLastLoginInfo("admin")).thenReturn(null);
        addMock("securityService", securityService);

        StorageService storageService = mock(StorageService.class);
        when(storageService.getStorageQuotaInfo(0)).thenReturn(null);
        addMock("storageService", storageService);

        //Creates a new WicketTester
        ArtifactoryApplication application = new ArtifactoryApplication();
        MockServletContext servletContext = new MockServletContext(application, null);
        servletContext.setAttribute(ArtifactoryContext.APPLICATION_CONTEXT_KEY, ContextHelper.get());
        tester = new WicketTester(application, servletContext);

        setupTest();
    }

    /**
     * Subclasses can use this method to provide the configuration needed by each test.
     */
    protected abstract void setupTest();

    /**
     * Adds mock to the mock application context.
     *
     * @param beanName The name of the mock bean.
     * @param mock     The mock object.
     */
    protected void addMock(String beanName, Object mock) {
        applicationContextMock.putBean(beanName, mock);
    }

    protected WicketTester getTester() {
        return tester;
    }
}
