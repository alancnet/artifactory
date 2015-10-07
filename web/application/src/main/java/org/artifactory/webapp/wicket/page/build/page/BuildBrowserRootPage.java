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

package org.artifactory.webapp.wicket.page.build.page;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.build.BuildInfoUtils;
import org.artifactory.build.BuildRun;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.util.DoesNotExistException;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;
import org.artifactory.webapp.wicket.page.browse.home.RememberPageBehavior;
import org.artifactory.webapp.wicket.page.build.panel.AllBuildsPanel;
import org.artifactory.webapp.wicket.page.build.panel.BuildBreadCrumbsPanel;
import org.artifactory.webapp.wicket.page.build.panel.BuildTabbedPanel;
import org.artifactory.webapp.wicket.page.build.panel.BuildsForNamePanel;
import org.artifactory.webapp.wicket.page.build.tabs.NoPermissionsTabPanel;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.artifactory.webapp.wicket.page.build.BuildBrowserConstants.*;

/**
 * The root page of the build browser.<p/>
 * Supports mounted paths. See <a href="https://cwiki.apache.org/WICKET/request-mapping.html">request-mapping</a>
 * <p/>
 *
 * @author Noam Y. Tenne
 */
public class BuildBrowserRootPage extends AuthenticatedPage {

    private static final Logger log = LoggerFactory.getLogger(BuildBrowserRootPage.class);

    public static final String CHILD_PANEL_ID = "panel";

    @SpringBean
    private BuildService buildService;

    @SpringBean
    private AuthorizationService authorizationService;

    private PageParameters pageParameters;

    /**
     * Main constructor. Displays content according to the given page parameters
     *
     * @param pageParameters Page parameters include with request
     */
    public BuildBrowserRootPage(PageParameters pageParameters) {
        add(new RememberPageBehavior());

        this.pageParameters = pageParameters;
        setOutputMarkupId(true);

        Panel panelToAdd = null;

        //Anonymous build info access was disabled
        if(authorizationService.isAnonUserAndAnonBuildInfoAccessDisabled()) {
            add(new NoPermissionsTabPanel(CHILD_PANEL_ID));
            add(new Label("buildBreadCrumbs",""));
            return;
        }
        try {
            if (!pageParameters.get(MODULE_ID).isEmpty()) {
                panelToAdd = getModuleSpecificTabbedPanel(null);

            } else if (!pageParameters.get(BUILD_STARTED).isEmpty() || !pageParameters.get(BUILD_NUMBER).isEmpty()) {
                /**
                 * If the URL was sent from Artifactory, it will include the build started param; but if it was sent by a
                 * user, it could contain only the build number
                 */
                panelToAdd = getTabbedPanel();

            } else if (!pageParameters.get(BUILD_NAME).isEmpty()) {
                panelToAdd = getBuildForNamePanel();
            }
        } catch (DoesNotExistException e) {
            panelToAdd = null;
            error(e.getMessage());

            //Clear all page parameters so that no breadcrumbs are added
            pageParameters.clearNamed();
        }

        if (panelToAdd == null) {
            panelToAdd = new AllBuildsPanel(CHILD_PANEL_ID);
        }

        add(panelToAdd);
        BuildBreadCrumbsPanel breadCrumbsPanel = new BuildBreadCrumbsPanel();
        add(breadCrumbsPanel);
        breadCrumbsPanel.addCrumbs(pageParameters);
    }

    /**
     * Returns the module-specific tabbed panel to display
     *
     * @param forcedModule ID module to display instead of a one that might be specified in the parameters
     * @return Module specific tabbed panel
     */
    private Panel getModuleSpecificTabbedPanel(String forcedModule) {
        String buildName = getBuildName();
        String buildNumber = getBuildNumber();
        String buildStarted = null;
        String moduleId;

        /**
         * If the forced module is specified, it means that the user entered a request with a module id, but no build
         * started parameter
         */
        if (StringUtils.isNotBlank(forcedModule)) {
            moduleId = forcedModule;
        } else {
            //Normal request from artifactory, containing all needed parameters
            buildStarted = getStringParameter(BUILD_STARTED);
            moduleId = getModuleId();
        }
        Build build = getBuild(buildName, buildNumber, buildStarted);
        Module module = getModule(build, moduleId);
        pageParameters.set(BUILD_STARTED, buildStarted);
        pageParameters.set(MODULE_ID, moduleId);
        return new BuildTabbedPanel(CHILD_PANEL_ID, build, module);
    }

    /**
     * Returns the general info tabbed panel to display
     *
     * @return General info tabbed panel
     */
    private Panel getTabbedPanel() {
        String buildName = getBuildName();
        String buildNumber = getBuildNumber();

        String buildStarted = null;
        /**
         * If the build started wasn't specified, the URL contains only a build number, which means we select to display
         * the latest build of the specified number
         */
        if (pageParameters.get(BUILD_STARTED).isEmpty()) {
            Build build = getBuild(buildName, buildNumber, buildStarted);
            pageParameters.set(BUILD_STARTED, build.getStarted());
            return new BuildTabbedPanel(CHILD_PANEL_ID, build, null);
        }

        buildStarted = getStringParameter(BUILD_STARTED);
        try {
            BuildInfoUtils.parseBuildTime(buildStarted);
            Build build = getBuild(buildName, buildNumber, buildStarted);
            return new BuildTabbedPanel(CHILD_PANEL_ID, build, null);
        } catch (IllegalArgumentException e) {
            /**
             * If the build started param was specified, but didn't parse properly, then the request contains a build
             * number And build module id, which means we select to display the specified module of the latest build of
             * the specified number
             */
            return getModuleSpecificTabbedPanel(buildStarted);
        }
    }

    /**
     * Returns the builds-for-name panel to display
     *
     * @return Builds-for-name panel
     */
    private Panel getBuildForNamePanel() {
        Panel panelToAdd = null;
        String buildName = getBuildName();
        try {
            Set<BuildRun> buildsByName = buildService.searchBuildsByName(buildName);

            if (buildsByName == null || buildsByName.isEmpty()) {
                String errorMessage = new StringBuilder().append("Could not find builds by name '").append(buildName).
                        append("'").toString();
                throwNotFoundError(errorMessage);
            }

            panelToAdd = new BuildsForNamePanel(CHILD_PANEL_ID, buildName, buildsByName);
        } catch (RepositoryRuntimeException e) {
            String errorMessage = new StringBuilder().append("Error locating builds by '").append(buildName).
                    append("': ").append(e.getMessage()).toString();
            throwInternalError(errorMessage);
        }

        return panelToAdd;
    }

    @Override
    public String getPageName() {
        return "Build Browser";
    }

    /**
     * Returns the module ID page parameter
     *
     * @return Module ID
     */
    private String getModuleId() {
        return getStringParameter(MODULE_ID);
    }

    /**
     * Returns the latest built build object for the given name and number
     *
     * @param buildName    Name of build to locate
     * @param buildNumber  Number of build to locate
     * @param buildStarted Started time of build to locate
     * @return Build object if found.
     * @throws AbortWithHttpErrorCodeException If the build was not found
     */
    private Build getBuild(String buildName, String buildNumber, String buildStarted) {
        boolean buildStartedSupplied = StringUtils.isNotBlank(buildStarted);
        try {
            Build build = null;
            if (buildStartedSupplied) {
                BuildRun buildRun = buildService.getBuildRun(buildName, buildNumber, buildStarted);
                if (buildRun != null) {
                    build = buildService.getBuild(buildRun);
                }
            } else {
                //Take the latest build of the specified number
                build = buildService.getLatestBuildByNameAndNumber(buildName, buildNumber);
            }
            if (build == null) {
                StringBuilder builder = new StringBuilder().append("Could not find build '").append(buildName).
                        append("' #").append(buildNumber);
                if (buildStartedSupplied) {
                    builder.append(" that started at ").append(buildStarted);
                }
                throwNotFoundError(builder.toString());
            }
            return build;
        } catch (RepositoryRuntimeException e) {
            String errorMessage = new StringBuilder().append("Error locating latest build for '").append(buildName).
                    append("' #").append(buildNumber).append(": ").append(e.getMessage()).toString();
            throwInternalError(errorMessage);
        }

        //Should not happen
        return null;
    }

    /**
     * Returns the module object of the given ID
     *
     * @param build    Build to search within
     * @param moduleId Module ID to locate
     * @return Module object if found.
     * @throws AbortWithHttpErrorCodeException If the module was not found
     */
    private Module getModule(Build build, String moduleId) {
        Module module = build.getModule(moduleId);

        if (module == null) {
            String errorMessage = new StringBuilder().append("Could not find module '").append(moduleId).
                    append("' within build '").append(build.getName()).append("' #").append(build.getNumber()).
                    toString();
            throwNotFoundError(errorMessage);
        }

        return module;
    }

    /**
     * Returns the build name page parameter
     *
     * @return Build name
     */
    protected String getBuildName() {
        return getStringParameter(BUILD_NAME);
    }

    /**
     * Returns the build number page parameter
     *
     * @return Build number
     */
    protected String getBuildNumber() {
        return getStringParameter(BUILD_NUMBER);
    }

    /**
     * Validates that the given key exists as a parameter key
     *
     * @param key Key to validate
     * @throws AbortWithHttpErrorCodeException If the key was not found
     */
    protected void validateKey(String key) {
        if (pageParameters.get(key).isEmpty()) {
            String errorMessage = new StringBuilder().append("Could not find parameter '").append(key).append("'").
                    toString();
            throwBadRequestError(errorMessage);
        }
    }

    /**
     * Returns the string value for the given key
     *
     * @param key Key of parameter to find
     * @return String value
     */
    protected String getStringParameter(String key) {
        validateKey(key);

        String value = pageParameters.get(key).toString();

        if (StringUtils.isBlank(value)) {
            String errorMessage = new StringBuilder().append("Blank value found for parameter '").append(key).
                    append("'").toString();
            throwBadRequestError(errorMessage);
        }

        return value;
    }

    /**
     * Throws a 404 AbortWithHttpErrorCodeException with the given message
     *
     * @param errorMessage Message to display in the error
     */
    protected void throwNotFoundError(String errorMessage) {
        logError(errorMessage);
        throw new DoesNotExistException(errorMessage);
    }

    /**
     * Throws a 400 AbortWithHttpErrorCodeException with the given message
     *
     * @param errorMessage Message to display in the error
     */
    protected void throwBadRequestError(String errorMessage) {
        throwError(HttpStatus.SC_BAD_REQUEST, errorMessage);
    }

    /**
     * Throws a 500 AbortWithHttpErrorCodeException with the given message
     *
     * @param errorMessage Message to display in the error
     */
    protected void throwInternalError(String errorMessage) {
        throwError(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage);
    }

    /**
     * Throws an AbortWithHttpErrorCodeException with the given status and message
     *
     * @param status       Status to set for error
     * @param errorMessage Message to display in the error
     */
    private void throwError(int status, String errorMessage) {
        logError(errorMessage);
        throw new AbortWithHttpErrorCodeException(status, errorMessage);
    }

    private void logError(String errorMessage) {
        log.error("An error occurred during the browsing of build info: {}", errorMessage);
    }
}