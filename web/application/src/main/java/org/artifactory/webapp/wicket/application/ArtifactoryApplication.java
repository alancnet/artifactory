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

import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import org.apache.wicket.authorization.strategies.CompoundAuthorizationStrategy;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.settings.IApplicationSettings;
import org.apache.wicket.settings.IMarkupSettings;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.settings.ISecuritySettings;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.locator.caching.CachingResourceStreamLocator;
import org.apache.wicket.util.time.Duration;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.BootstrapListener;
import org.artifactory.addon.wicket.SamlAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.artifactory.common.wicket.application.AddWicketPathListener;
import org.artifactory.common.wicket.application.NoLocaleResourceStreamLocator;
import org.artifactory.common.wicket.application.ResponsePageSupport;
import org.artifactory.common.wicket.component.panel.sidemenu.SiteMapAware;
import org.artifactory.common.wicket.contributor.ResourcePackage;
import org.artifactory.common.wicket.model.sitemap.MenuNode;
import org.artifactory.common.wicket.model.sitemap.SiteMap;
import org.artifactory.common.wicket.model.sitemap.SiteMapBuilder;
import org.artifactory.webapp.spring.ArtifactorySpringComponentInjector;
import org.artifactory.webapp.wicket.application.sitemap.ArtifactorySiteMapBuilder;
import org.artifactory.webapp.wicket.page.base.BasePage;
import org.artifactory.webapp.wicket.page.browse.listing.ArtifactListPage;
import org.artifactory.webapp.wicket.page.browse.simplebrowser.SimpleRepoBrowserPage;
import org.artifactory.webapp.wicket.page.build.BuildBrowserConstants;
import org.artifactory.webapp.wicket.page.build.page.BuildBrowserRootPage;
import org.artifactory.webapp.wicket.page.error.AccessDeniedPage;
import org.artifactory.webapp.wicket.page.error.InternalErrorPage;
import org.artifactory.webapp.wicket.page.error.PageExpiredErrorPage;
import org.artifactory.webapp.wicket.page.home.HomePage;
import org.artifactory.webapp.wicket.page.search.artifact.ArtifactSearchPage;
import org.artifactory.webapp.wicket.page.security.login.LoginPage;
import org.artifactory.webapp.wicket.page.security.login.LogoutPage;
import org.artifactory.webapp.wicket.page.security.login.forgot.ForgotPasswordPage;
import org.artifactory.webapp.wicket.page.security.login.reset.ResetPasswordPage;
import org.artifactory.webapp.wicket.page.security.profile.ProfilePage;
import org.artifactory.webapp.wicket.resource.LogoResource;
import org.artifactory.webapp.wicket.service.authentication.LogoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author Yoav Landman
 */
public class ArtifactoryApplication extends AuthenticatedWebApplication implements SiteMapAware {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryApplication.class);
    private static final String SHARED_RESOURCES_PATH = "wicket/resource/";

    @SpringBean
    private CentralConfigService centralConfig;

    @SpringBean
    private RepositoryService repositoryService;

    @SpringBean
    private AddonsManager addons;

    private SiteMap siteMap;

    /**
     * Used to prevent logo caching in browsers
     */
    private long logoModifyTime;

    private final EnumSet<ConstantValues> modes =
            Sets.newEnumSet(Collections.<ConstantValues>emptySet(), ConstantValues.class);

    public static ArtifactoryApplication get() {
        return (ArtifactoryApplication) Application.get();
    }

    public void mountPage(Class<? extends Page> pageClass) {
        String url = "/" + pageClass.getSimpleName().replaceFirst("Page", "").toLowerCase(Locale.ENGLISH) + ".html";
        safeMountPage(url, pageClass);
    }

    private void safeMountPage(String url, Class<? extends Page> pageClass) {
        unmount(url);   // un-mount first (in case of re-mounting)
        mountPage(url, pageClass);
    }

    /**
     * Updates the logo last modify time to force browsers to reload the logo.
     */
    public void updateLogo() {
        this.logoModifyTime = new File(ContextHelper.get().getArtifactoryHome().getLogoDir(), "logo").lastModified();
    }

    @Override
    public Class<? extends BasePage> getHomePage() {
        return HomePage.class;
    }

    @Override
    public RuntimeConfigurationType getConfigurationType() {
        //Init the modes from the constants if needed
        if (modes.isEmpty()) {
            // use configuration from the servlet context since properties are not bound to the thread when this method is called
            ArtifactoryHome artifactoryHome = getArtifactoryContext().getArtifactoryHome();
            ArtifactorySystemProperties artifactorySystemProperties = artifactoryHome.getArtifactoryProperties();
            /*if (Boolean.parseBoolean(artifactorySystemProperties.getProperty(ConstantValues.dev))) {
                modes.add(ConstantValues.dev);
            }*/
            if (Boolean.parseBoolean(artifactorySystemProperties.getProperty(ConstantValues.test))) {
                modes.add(ConstantValues.test);
            }
            if (Boolean.parseBoolean(artifactorySystemProperties.getProperty(ConstantValues.qa))) {
                modes.add(ConstantValues.qa);
            }
        }
        if (modes.contains(ConstantValues.dev)) {
            return RuntimeConfigurationType.DEVELOPMENT;
        } else {
            return super.getConfigurationType();
        }
    }

    public CentralConfigService getCentralConfig() {
        return centralConfig;
    }

    public CompoundAuthorizationStrategy getAuthorizationStrategy() {
        IAuthorizationStrategy authorizationStrategy = getSecuritySettings().getAuthorizationStrategy();
        if (!(authorizationStrategy instanceof CompoundAuthorizationStrategy)) {
            throw new IllegalStateException(
                    "Unexpected authorization strategy: " + authorizationStrategy.getClass());
        }
        return (CompoundAuthorizationStrategy) authorizationStrategy;
    }

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    @Override
    public SiteMap getSiteMap() {
        return siteMap;
    }

    public String getSharedResourcesPath() {
        return SHARED_RESOURCES_PATH;
    }

    public boolean isLogoExists() {
        return logoModifyTime != 0;
    }

    public long getLogoModifyTime() {
        return logoModifyTime;
    }

    boolean isDevelopmentMode() {
        return RuntimeConfigurationType.DEVELOPMENT.equals(getConfigurationType());
    }

    @Override
    protected void onDestroy() {
        deleteUploadsFolder();
        super.onDestroy();
    }

    @Override
    protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
        return ArtifactoryWebSession.class;
    }

    @Override
    protected Class<? extends WebPage> getSignInPageClass() {
        SamlAddon samlAddon = addons.addonByType(SamlAddon.class);
        if (samlAddon.isAutoRedirectToSamlIdentityProvider()) {
            return samlAddon.getSamlLoginRequestPageClass();
        }

        // return default login page
        return LoginPage.class;
    }

    @Override
    protected WebResponse newWebResponse(WebRequest webRequest, HttpServletResponse httpServletResponse) {
        return new IgnoreEofWebResponse(
                new HeaderBufferingWebResponse(super.newWebResponse(webRequest, httpServletResponse)));
    }

    @Override
    protected WebRequest newWebRequest(HttpServletRequest servletRequest, final String filterPath) {
        return new ArtifactoryWebRequest(servletRequest, filterPath);
    }

    @Override
    protected void init() {
        setupSpring();

        // bind context
        ArtifactoryContext originalContext = ContextHelper.get();
        boolean ctxAlreadyBound = originalContext != null;
        ArtifactoryContext context = getArtifactoryContext();
        if (!ctxAlreadyBound) {
            ArtifactoryContextThreadBinder.bind(context);
        } else {
            if (context != originalContext) {
                throw new IllegalStateException(
                        "Initialization of Wicket Application with a Spring context " + context +
                                " different from the Thread bound one " + originalContext);
            }
        }

        boolean artifactoryHomeAlreadyBound = ArtifactoryHome.isBound();
        if (!artifactoryHomeAlreadyBound) {
            ArtifactoryHome.bind(context.getArtifactoryHome());
        }

        super.init();

        try {
            doInit();
        } finally {
            // unbind context
            if (!ctxAlreadyBound) {
                ArtifactoryContextThreadBinder.unbind();
            }
            if (!artifactoryHomeAlreadyBound) {
                ArtifactoryHome.unbind();
            }
        }
    }

    /**
     * Rebuild the site map and re-mount the pages.
     */
    public void rebuildSiteMap() {
        buildSiteMap();
        mountPages();
    }

    private void setupSpring() {
        getComponentInstantiationListeners().add(new ArtifactorySpringComponentInjector(this));
        Injector.get().inject(this);
    }

    /**
     * Mount a resource.
     *
     * @param path     The path of the resource.
     * @param resource The resource itself
     */
    private void mountResource(String path, final IResource resource) {
        mountResource(path, new ResourceReference(path) {
            @Override
            public IResource getResource() {
                return resource;
            }
        });
    }

    private void mountLogo() {
        mountResource("/logo", new LogoResource(ContextHelper.get().getArtifactoryHome()));
    }

    private void notifyBootstrapListeners() {
        ArtifactoryContext artifactoryContext = getArtifactoryContext();
        Map<String, BootstrapListener> beanMap = artifactoryContext.beansForType(BootstrapListener.class);
        for (BootstrapListener listener : beanMap.values()) {
            listener.onApplicationInit();
        }
    }

    private ArtifactoryContext getArtifactoryContext() {
        return (ArtifactoryContext) getServletContext()
                .getAttribute(ArtifactoryContext.APPLICATION_CONTEXT_KEY);
    }

    /**
     * Delete the upload folder (in case we were not shut down cleanly)
     */
    private void deleteUploadsFolder() {
        ArtifactoryHome artifactoryHome = getArtifactoryContext().getArtifactoryHome();
        File tmpUploadsDir = artifactoryHome.getTempUploadDir();
        if (tmpUploadsDir.exists()) {
            try {
                FileUtils.cleanDirectory(tmpUploadsDir);
            } catch (IOException ignore) {
                log.warn("Failed to delete the upload directory.");
            }
        }
    }

    private void setupListeners() {
        // wrap the unauthorizedComponentInstantiation listener so that we can discard repoPath
        // attributes from the request when needing to login
        ISecuritySettings securitySettings = getSecuritySettings();
        IUnauthorizedComponentInstantiationListener orig =
                securitySettings.getUnauthorizedComponentInstantiationListener();

        securitySettings.setUnauthorizedComponentInstantiationListener(
                new RepoBrowsingAwareUnauthorizedComponentInstantiationListener(orig));

        // add ArtifactoryRequestCycleListener
        getRequestCycleListeners().add(new ArtifactoryRequestCycleListener());
        getRequestCycleListeners().add(new ResponsePageSupport());
    }

    private void mountPages() {
        Set<Class<? extends Page>> hardMountPages = Sets.newHashSet();

        hardMountPage(hardMountPages, SimpleRepoBrowserPage.PATH, SimpleRepoBrowserPage.class);
        hardMountPage(hardMountPages, ArtifactListPage.PATH, ArtifactListPage.class);

        // mount services
        hardMountPage(hardMountPages, "/service/logout", LogoutService.class);

        // mount general pages
        mountPage(InternalErrorPage.class);
        mountPage(AccessDeniedPage.class);
        mountPage(PageExpiredErrorPage.class);

        mountPage(LoginPage.class);
        mountPage(LogoutPage.class);
        mountPage(ProfilePage.class);
        mountPage(ResetPasswordPage.class);
        mountPage(ForgotPasswordPage.class);

        hardMountPage(hardMountPages, "/search/artifact", ArtifactSearchPage.class);
        safeMountPage("/search/artifact/", ArtifactSearchPage.class);

        SamlAddon samlAddon = addons.addonByType(SamlAddon.class);
        Class<? extends Page> loginRequest = samlAddon.getSamlLoginRequestPageClass();
        if (loginRequest != null) {
            hardMountPage(hardMountPages, "/saml/loginRequest", loginRequest);
        }
        Class<? extends Page> loginResponse = samlAddon.getSamlLoginResponsePageClass();
        if (loginResponse != null) {
            hardMountPage(hardMountPages, "/saml/loginResponse", loginResponse);
        }
        Class<? extends Page> logoutRequest = samlAddon.getSamlLogoutRequestPageClass();
        if (logoutRequest != null) {
            hardMountPage(hardMountPages, "/saml/logoutRequest", logoutRequest);
        }

        // We need both of these since accessing modules without the trailing "/" mount fails
        hardMountPage(hardMountPages, BuildBrowserConstants.MOUNT_PATH, BuildBrowserRootPage.class);
        safeMountPage(BuildBrowserConstants.MOUNT_PATH + "/", BuildBrowserRootPage.class);

        for (MenuNode pageNode : siteMap.getPages()) {
            if (!hardMountPages.contains(pageNode.getPageClass())) {
                mountPage(pageNode.getPageClass());
            }
        }
    }

    private void hardMountPage(Set<Class<? extends Page>> hardMountPages, String url, Class<? extends Page> pageClass) {
        safeMountPage(url, pageClass);
        hardMountPages.add(pageClass);
    }

    private void buildSiteMap() {
        SiteMapBuilder builder = newSiteMapBuilder();
        builder.buildSiteMap();
        builder.cachePageNodes();
        siteMap = builder.getSiteMap();
    }

    protected void doInit() {
        setup();

        buildSiteMap();
        mountPages();
        mountLogo();

        deleteUploadsFolder();
        notifyBootstrapListeners();
        updateLogo();
    }

    protected void setup() {
        setupListeners();

        // set HeaderRenderStrategy = ParentFirstHeaderRenderStrategy
        System.setProperty("Wicket_HeaderRenderStrategy",
                "org.apache.wicket.markup.renderStrategy.ParentFirstHeaderRenderStrategy");

        // look for pages at the root of the web-app
        IResourceSettings resourceSettings = getResourceSettings();
        resourceSettings.addResourceFolder("");
        IPackageResourceGuard packageResourceGuard = resourceSettings.getPackageResourceGuard();
        if (packageResourceGuard instanceof SecurePackageResourceGuard) {
            SecurePackageResourceGuard resourceGuard = (SecurePackageResourceGuard) packageResourceGuard;
            resourceGuard.addPattern("+Icon");
        }

        // ResourcePackage resources are locale insensitive
        NoLocaleResourceStreamLocator locator = new NoLocaleResourceStreamLocator();
        locator.addNoLocaleClass(ResourcePackage.class);
        resourceSettings.setResourceStreamLocator(new CachingResourceStreamLocator(locator));

        // add the addons authorization strategy
        AddonsAuthorizationStrategy addonsAuthorizationStrategy = new AddonsAuthorizationStrategy();
        Injector.get().inject(addonsAuthorizationStrategy);
        getAuthorizationStrategy().add(addonsAuthorizationStrategy);

        // increase request timeout to support long running transactions
        IRequestCycleSettings requestCycleSettings = getRequestCycleSettings();
        requestCycleSettings.setTimeout(Duration.hours(5));

        // set error pages
        IApplicationSettings applicationSettings = getApplicationSettings();
        applicationSettings.setPageExpiredErrorPage(PageExpiredErrorPage.class);
        applicationSettings.setAccessDeniedPage(AccessDeniedPage.class);
        applicationSettings.setInternalErrorPage(InternalErrorPage.class);

        // markup settings
        IMarkupSettings markupSettings = getMarkupSettings();
        markupSettings.setDefaultMarkupEncoding("UTF-8");
        markupSettings.setCompressWhitespace(true);
        markupSettings.setStripComments(true);
        markupSettings.setStripWicketTags(true);

        //QA settings
        if (modes.contains(ConstantValues.qa)) {
            getComponentInstantiationListeners().add(new AddWicketPathListener());
        }

        // RTFACT-4619, fixed by patching HeaderBufferingWebResponse
        getRequestCycleSettings().setBufferResponse(false);

        // RTFACT-4636
        getPageSettings().setVersionPagesByDefault(false);
    }

    protected SiteMapBuilder newSiteMapBuilder() {
        SiteMapBuilder builder = new ArtifactorySiteMapBuilder();
        Injector.get().inject(builder);
        return builder;
    }
}