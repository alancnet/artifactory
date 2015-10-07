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

package org.artifactory.webapp.servlet.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.log.BootstrapLogger;
import org.artifactory.log.logback.LogbackContextHelper;
import org.artifactory.log.logback.LogbackContextSelector;
import org.artifactory.log.logback.LoggerConfigInfo;
import org.artifactory.util.FileWatchDog;
import org.artifactory.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Configured logback with the config file from etc directory.
 *
 * @author Yossi Shaul
 * @author Yoav Landman
 */
public class LogbackConfigListener implements ServletContextListener {

    private ArtifactoryHome home;
    LogbackConfigWatchDog configWatchDog;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        home = (ArtifactoryHome) servletContext.getAttribute(ArtifactoryHome.SERVLET_CTX_ATTR);
        if (home == null) {
            throw new IllegalStateException("Artifactory home not initialized");
        }

        //Install the juli to slf4j bridge (disabled dur to RTFACT-1283)
        //SLF4JBridgeHandler.install();

        boolean selectorUsed = System.getProperty("logback.ContextSelector") != null;
        LoggerContext context;
        if (selectorUsed) {
            String contextId = HttpUtils.getContextId(servletContext);
            LoggerConfigInfo configInfo = new LoggerConfigInfo(contextId, home);
            LogbackContextSelector.bindConfig(configInfo);
            try {
                //This load should already use a context from the selector
                context = getOrInitLoggerContext();
            } finally {
                LogbackContextSelector.unbindConfig();
            }
        } else {
            context = getOrInitLoggerContext();
            LogbackContextHelper.configure(context, home);
        }

        //Configure and start the watchdog
        configWatchDog = new LogbackConfigWatchDog(context, servletContext);
        configureWatchdog(servletContext);
        configWatchDog.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        configWatchDog.interrupt();
        ContextSelector selector = ContextSelectorStaticBinder.getSingleton().getContextSelector();
        String contextId = HttpUtils.getContextId(sce.getServletContext());
        selector.detachLoggerContext(contextId);
        configWatchDog.loggerContext.stop();
    }

    private static LoggerContext getOrInitLoggerContext() {
        return (LoggerContext) LoggerFactory.getILoggerFactory();
    }

    private void configureWatchdog(ServletContext servletContext) {
        String intervalString = servletContext.getInitParameter("logbackRefreshInterval");
        if (intervalString != null) {
            try {
                long refreshInterval = Long.parseLong(intervalString);
                configWatchDog.setDelay(refreshInterval);
            } catch (NumberFormatException e) {
                BootstrapLogger.error("Failed to parse logbackRefreshInterval. Log refresh will not be active.");
                getOrInitLoggerContext();
            }
        }
    }

    private class LogbackConfigWatchDog extends FileWatchDog {

        private final Logger log = LoggerFactory.getLogger(LogbackConfigListener.LogbackConfigWatchDog.class);

        private LoggerContext loggerContext;
        private ServletContext servletContext;
        private final boolean selectorUsed;

        public LogbackConfigWatchDog(LoggerContext loggerContext, ServletContext servletContext) {
            super(home.getLogbackConfig(), false);
            setName("logback-watchdog");
            this.selectorUsed = System.getProperty("logback.ContextSelector") != null;
            this.servletContext = servletContext;
            this.loggerContext = loggerContext;
            checkAndConfigure();
        }

        @Override
        protected void doOnChange() {
            String contextId = null;
            if (selectorUsed) {
                // if the selector is used, then bind a new LoggerConfigInfo.
                // see JFW-1180
                contextId = HttpUtils.getContextId(servletContext);
                bind(contextId);
            }
            try {
                LogbackContextHelper.configure(loggerContext, home, contextId);
                //Log after re-config, since this class logger is constructed before config with the default warn level
                log.info("Reloaded logback config from: {}.", file.getAbsolutePath());
            } finally {
                if (selectorUsed) {
                    unbind();
                }
            }
        }

        private void bind(String contextId) {
            LoggerConfigInfo configInfo = new LoggerConfigInfo(contextId, home);
            LogbackContextSelector.bindConfig(configInfo);
        }

        private void unbind() {
            LogbackContextSelector.unbind();
        }
    }
}
