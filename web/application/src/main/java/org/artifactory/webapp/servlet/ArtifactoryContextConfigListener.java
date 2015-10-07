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

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.ha.HaNodeProperties;
import org.artifactory.converters.ConverterManager;
import org.artifactory.converters.VersionProviderImpl;
import org.artifactory.file.lock.ArtifactoryLockFile;
import org.artifactory.log.logback.LogbackContextSelector;
import org.artifactory.log.logback.LoggerConfigInfo;
import org.artifactory.spring.SpringConfigPaths;
import org.artifactory.spring.SpringConfigResourceLoader;
import org.artifactory.util.ExceptionUtils;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.ResourceUtils;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ArtifactoryContextConfigListener implements ServletContextListener {

    private static final String LOCK_FILENAME = ".lock";

    private ArtifactoryLockFile artifactoryLockFile;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        final ServletContext servletContext = event.getServletContext();

        setSessionTrackingMode(servletContext);

        final Thread initThread = new Thread("art-init") {
            boolean success = true;

            @SuppressWarnings({"unchecked"})
            @Override
            public void run() {
                try {
                    //Use custom logger
                    String contextId = HttpUtils.getContextId(servletContext);
                    //Build a partial config, since we expect the logger-context to exit in the selector cache by only contextId
                    LoggerConfigInfo configInfo = new LoggerConfigInfo(contextId);
                    LogbackContextSelector.bindConfig(configInfo);
                    //No log field since needs to lazy initialize only after logback customization listener has run
                    Logger log = getLogger();
                    configure(servletContext, log);

                    LogbackContextSelector.unbindConfig();
                } catch (Exception e) {
                    getLogger().error("Application could not be initialized: " +
                            ExceptionUtils.getRootCause(e).getMessage(), e);
                    success = false;
                } finally {
                    if (success) {
                        //Run the waiting filters
                        BlockingQueue<DelayedInit> waitingFiltersQueue = (BlockingQueue<DelayedInit>) servletContext
                                .getAttribute(DelayedInit.APPLICATION_CONTEXT_LOCK_KEY);
                        List<DelayedInit> waitingInits = new ArrayList<>();
                        waitingFiltersQueue.drainTo(waitingInits);
                        for (DelayedInit filter : waitingInits) {
                            try {
                                filter.delayedInit();
                            } catch (ServletException e) {
                                getLogger().error("Could not init {}.", filter.getClass().getName(), e);
                                success = false;
                                break;
                            }
                        }
                    }
                    //Remove the lock and open the app to requests
                    servletContext.removeAttribute(DelayedInit.APPLICATION_CONTEXT_LOCK_KEY);
                }
            }
        };
        initThread.setDaemon(true);
        servletContext.setAttribute(DelayedInit.APPLICATION_CONTEXT_LOCK_KEY, new LinkedBlockingQueue<DelayedInit>());
        initThread.start();
        if (Boolean.getBoolean("artifactory.init.useServletContext")) {
            try {
                getLogger().info("Waiting for servlet context initialization ...");
                initThread.join();
            } catch (InterruptedException e) {
                getLogger().error("Artifactory initialization thread got interrupted", e);
            }
        }
    }

    /**
     * Disable sessionId in URL (Servlet 3.0 containers) by setting the session tracking mode to SessionTrackingMode.COOKIE
     * For Servlet container < 3.0 we use different method (for tomcat 6 we use custom context.xml and for jetty
     * there is a custom jetty.xml file).
     */
    @SuppressWarnings("unchecked")
    private void setSessionTrackingMode(ServletContext servletContext) {
        // Only for Servlet container version 3.0 and above
        if (servletContext.getMajorVersion() < 3) {
            return;
        }

        // We cannot use ConstantValue.enableURLSessionId.getBoolean() since ArtifactoryHome is not binded yet.
        ArtifactoryHome artifactoryHome = (ArtifactoryHome) servletContext.getAttribute(
                ArtifactoryHome.SERVLET_CTX_ATTR);
        if (artifactoryHome == null) {
            throw new IllegalStateException("Artifactory home not initialized.");
        }

        if (artifactoryHome.getArtifactoryProperties().getBooleanProperty(ConstantValues.supportUrlSessionTracking)) {
            getLogger().debug("Skipping setting session tracking mode to COOKIE, enableURLSessionId flag it on.");
            return;
        }

        try {
            // load enum with reflection
            ClassLoader cl = ClassUtils.getDefaultClassLoader();
            Class<Enum> trackingModeEnum = (Class<Enum>) cl.loadClass("javax.servlet.SessionTrackingMode");
            Enum cookieTrackingMode = Enum.valueOf(trackingModeEnum, "COOKIE");

            // reflective call servletContext.setSessionTrackingModes(trackingModes)
            Method method = servletContext.getClass().getMethod("setSessionTrackingModes", Set.class);
            method.setAccessible(true);
            ReflectionUtils.invokeMethod(method, servletContext, Sets.newHashSet(cookieTrackingMode));
            getLogger().debug("Successfully set session tracking mode to COOKIE");
        } catch (Exception e) {
            getLogger().warn("Failed to set session tracking mode: " + e.getMessage());
        }
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(ArtifactoryContextConfigListener.class);
    }

    private void configure(ServletContext servletContext, Logger log) throws Exception {
        long start = System.currentTimeMillis();

        ArtifactoryHome artifactoryHome =
                (ArtifactoryHome) servletContext.getAttribute(ArtifactoryHome.SERVLET_CTX_ATTR);
        VersionProviderImpl versionProvider = (VersionProviderImpl) servletContext.getAttribute(
                ArtifactoryHome.ARTIFACTORY_VERSION_PROVIDER_OBJ);
        ConverterManager converterManager = (ConverterManager) servletContext.getAttribute(
                ArtifactoryHome.ARTIFACTORY_CONVERTER_OBJ);

        if (artifactoryHome == null) {
            throw new IllegalStateException("Artifactory home not initialized.");
        }
        CompoundVersionDetails runningVersionDetails = versionProvider.getRunning();

        logAsciiArt(log, artifactoryHome, runningVersionDetails);

        ApplicationContext context;
        try {
            ArtifactoryHome.bind(artifactoryHome);

            //todo consider moving to org.artifactory.webapp.servlet.ArtifactoryHomeConfigListener.contextInitialized()
            if (artifactoryHome.isHaConfigured()) {
                log.debug("Not using Artifactory lock file on HA environment");
            } else {
                artifactoryLockFile = new ArtifactoryLockFile(new File(artifactoryHome.getDataDir(), LOCK_FILENAME));
                artifactoryLockFile.tryLock();
            }

            Class<?> contextClass = ClassUtils.forName(
                    "org.artifactory.spring.ArtifactoryApplicationContext", ClassUtils.getDefaultClassLoader());
            Constructor<?> constructor = contextClass.
                    getConstructor(String.class, SpringConfigPaths.class, ArtifactoryHome.class,
                            ConverterManager.class, VersionProviderImpl.class);
            //Construct the context name based on the context path
            //(will not work with multiple servlet containers on the same vm!)
            String contextUniqueName = HttpUtils.getContextId(servletContext);
            SpringConfigPaths springConfigPaths = SpringConfigResourceLoader.getConfigurationPaths(artifactoryHome);
            context = (ApplicationContext) constructor.newInstance(
                    contextUniqueName, springConfigPaths, artifactoryHome, converterManager, versionProvider);
        } finally {
            ArtifactoryHome.unbind();
        }
        log.info("\n" +
                "###########################################################\n" +
                "### Artifactory successfully started (" +
                String.format("%-17s", (DurationFormatUtils.formatPeriod(start, System.currentTimeMillis(), "s.S")) +
                        " seconds)") + " ###\n" +
                "###########################################################\n");

        //Register the context for easy retrieval for faster destroy
        servletContext.setAttribute(ArtifactoryContext.APPLICATION_CONTEXT_KEY, context);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        AbstractApplicationContext context = (AbstractApplicationContext) event.getServletContext().getAttribute(
                ArtifactoryContext.APPLICATION_CONTEXT_KEY);
        try {
            getLogger().debug("Context shutdown started");
            if (context != null) {
                if (context instanceof ArtifactoryContext) {
                    AddonsManager addonsManager = ((ArtifactoryContext) context).beanForType(AddonsManager.class);
                    addonsManager.addonByType(HaCommonAddon.class).shutdown();
                }
                context.destroy();
            }
            if (artifactoryLockFile != null) {
                artifactoryLockFile.release();
            }
            getLogger().debug("Context shutdown Finished");
        } finally {
            event.getServletContext().removeAttribute(ArtifactoryContext.APPLICATION_CONTEXT_KEY);
            event.getServletContext().removeAttribute(ArtifactoryHome.SERVLET_CTX_ATTR);
        }
    }

    private void logAsciiArt(Logger log, ArtifactoryHome artifactoryHome, CompoundVersionDetails versionDetails) {
        String message = null;
        try {
            message = buildEditionMessage(artifactoryHome, versionDetails);
        } catch (Exception e) {
            log.warn("Failed to detect edition {}", e.getMessage());
            log.debug("Failed to detect edition {}", e.getMessage(), e);
        }
        if (message == null) {
            // build the generic version string (no edition details
            buildNoEditionMessage(versionDetails);
        }

        // artifactory home location
        message += " Artifactory Home: '" + artifactoryHome.getHomeDir().getAbsolutePath() + "'\n";

        //optionally log HA properties
        if (artifactoryHome.isHaConfigured()) {
            HaNodeProperties haNodeProperties = artifactoryHome.getHaNodeProperties();
            if (haNodeProperties != null) {
                message += " Artifactory Cluster Home: '" + haNodeProperties.getClusterHome().getAbsolutePath()
                        + "'\n HA Node ID: '" + haNodeProperties.getServerId() + "'\n";
            }
        }
        log.info(message);
    }

    private String buildEditionMessage(ArtifactoryHome artifactoryHome, CompoundVersionDetails version)
            throws Exception {
        ArtifactoryEdition runningEdition = ArtifactoryEdition.detect(artifactoryHome);
        InputStream resource = ResourceUtils.getResource("/ascii-editions.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, Charsets.UTF_8))) {
            String edition;
            while ((edition = reader.readLine()) != null) {
                if (!runningEdition.name().equalsIgnoreCase(edition)) {
                    // skip 8 lines to get to the next chunk
                    for (int i = 0; i < 8; i++) {
                        reader.readLine();
                    }

                } else {
                    StringBuilder sb = new StringBuilder("\n");
                    // height of the ascii art with no modifications
                    for (int i = 0; i < 6; i++) {
                        sb.append(reader.readLine()).append("\n");
                    }
                    // line 7 prepend the version
                    String versionLine = reader.readLine();
                    String versionStr = " Version:  " + version.getVersionName();
                    sb.append(versionStr).append(versionLine.substring(versionStr.length())).append("\n");

                    // line 8 prepend the revision
                    String revisionLine = reader.readLine();
                    String revisionStr = " Revision: " + version.getRevision();
                    sb.append(revisionStr).append(revisionLine.substring(revisionStr.length())).append("\n");

                    return sb.toString();
                }
            }
        }
        throw new IllegalArgumentException("Couldn't find matching art for " + runningEdition);
    }

    private String buildNoEditionMessage(CompoundVersionDetails versionDetails) {
        String msg =
                "\n" +
                        "               _   _  __           _\n" +
                        "    /\\        | | (_)/ _|         | |\n" +
                        "   /  \\   _ __| |_ _| |_ __ _  ___| |_ ___  _ __ _   _\n" +
                        "  / /\\ \\ | '__| __| |  _/ _` |/ __| __/ _ \\| '__| | | |\n" +
                        " / ____ \\| |  | |_| | || (_| | (__| || (_) | |  | |_| |\n" +
                        "/_/    \\_\\_|   \\__|_|_| \\__,_|\\___|\\__\\___/|_|   \\__, |\n" +
                        String.format(" Version:  %-39s__/ |\n", versionDetails.getVersionName()) +
                        String.format(" Revision: %-38s|___/\n", versionDetails.getRevision());
        return msg;
    }
}
