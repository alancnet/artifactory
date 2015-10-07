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

package org.artifactory.webapp.servlet;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converters.ConvertersManagerImpl;
import org.artifactory.converters.VersionProviderImpl;
import org.artifactory.log.BootstrapLogger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

/**
 * @author yoavl
 */
public class ArtifactoryHomeConfigListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        String artHomeCtx = servletContext.getInitParameter(ArtifactoryHome.SYS_PROP);
        ArtifactoryHome artifactoryHome;
        if (artHomeCtx != null) {
            // Use home dir from Context
            artifactoryHome = new ArtifactoryHome(new File(artHomeCtx));
        } else {
            // Initialize home dir using default behavior
            artifactoryHome = new ArtifactoryHome(new ServletLogger(servletContext));
        }
        BootstrapLogger.info("Starting Artifactory [artifactory.home=" +
                artifactoryHome.getHomeDir().getAbsolutePath() + "].");
        // add the artifactory home to the servlet context
        servletContext.setAttribute(ArtifactoryHome.SERVLET_CTX_ATTR, artifactoryHome);
        VersionProviderImpl versionProvider = new VersionProviderImpl(artifactoryHome);
        ConvertersManagerImpl convertersManager = new ConvertersManagerImpl(artifactoryHome, versionProvider);
        convertersManager.convertHomes();
        // add the converterManager to the servlet context
        servletContext.setAttribute(ArtifactoryHome.ARTIFACTORY_CONVERTER_OBJ, convertersManager);
        servletContext.setAttribute(ArtifactoryHome.ARTIFACTORY_VERSION_PROVIDER_OBJ, versionProvider);
        // Init System properties
        artifactoryHome.initAndLoadSystemPropertyFile();
        // Init and load Mimetypes
        artifactoryHome.initAndLoadMimeTypes();
    }

    private static class ServletLogger implements ArtifactoryHome.SimpleLog {
        private final ServletContext servletContext;

        private ServletLogger(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        @Override
        public void log(String message) {
            servletContext.log(message);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
