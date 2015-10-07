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

package org.artifactory.log.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Yoav Landman
 */
public class LogbackContextSelector implements ContextSelector {

    private final ConcurrentMap<String, LoggerContext> loggerContextsByContextId;
    private final LoggerContext defaultContext;

    private static final ThreadLocal<LoggerContext> tlsLoggingContext = new ThreadLocal<>();
    private static final ThreadLocal<LoggerConfigInfo> tlsConfigInfo = new ThreadLocal<>();

    public LogbackContextSelector(LoggerContext context) {
        loggerContextsByContextId = new ConcurrentHashMap<>();
        defaultContext = context;
    }

    @Override
    public LoggerContext getDefaultLoggerContext() {
        return defaultContext;
    }

    @Override
    public LoggerContext detachLoggerContext(String loggerContextName) {
        return loggerContextsByContextId.remove(loggerContextName);
    }

    @Override
    public LoggerContext getLoggerContext() {
        //First check if ThreadLocal has been set already
        LoggerContext loggerContext = tlsLoggingContext.get();
        if (loggerContext != null) {
            return loggerContext;
        }
        LoggerConfigInfo configInfo = getConfigInfo();
        if (configInfo != null) {
            //Try to get it from the cache by contextPath
            String contextPath = configInfo.getContextId();
            loggerContext = loggerContextsByContextId.get(contextPath);
            if (loggerContext == null) {
                //We have to create a new LoggerContext
                loggerContext = new LoggerContext();
                loggerContext.setName(contextPath);
                ArtifactoryHome home = configInfo.getHome();
                if (home == null) {
                    throw new IllegalStateException(
                            "Trying to create a new configuration but artifactory home is null.");
                }
                LogbackContextHelper.configure(loggerContext, home, configInfo.getContextId());
                LoggerContext existingContext = loggerContextsByContextId.putIfAbsent(contextPath, loggerContext);
                return existingContext == null ? loggerContext : existingContext;
            } else {
                return loggerContext;
            }
        } else {
            //Return the default context
            return defaultContext;
        }
    }

    @Override
    public List<String> getContextNames() {
        List<String> list = new ArrayList<>();
        list.addAll(loggerContextsByContextId.keySet());
        return list;
    }

    @Override
    public LoggerContext getLoggerContext(String name) {
        return loggerContextsByContextId.get(name);
    }

    /**
     * Returns the number of managed contexts Used for testing purposes
     *
     * @return the number of managed contexts
     */
    public int getCount() {
        return loggerContextsByContextId.size();
    }

    public static void bind() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        ContextSelector selector = ContextSelectorStaticBinder.getSingleton().getContextSelector();
        if (selector instanceof LogbackContextSelector) {
            tlsLoggingContext.set(context);
        }
    }

    public static void unbind() {
        tlsLoggingContext.remove();
    }

    public static void bindConfig(LoggerConfigInfo info) {
        tlsConfigInfo.set(info);
    }

    public static void unbindConfig() {
        tlsConfigInfo.remove();
    }

    /**
     * Try to get a thread bound config (set during initialization). If there is no config info on the thread try
     * creating it from artifactory home
     */
    private LoggerConfigInfo getConfigInfo() {
        LoggerConfigInfo configInfo = tlsConfigInfo.get();
        if (configInfo == null) {
            ArtifactoryContext context = ContextHelper.get();
            if (context != null) {
                configInfo = new LoggerConfigInfo(context);
            }
        }
        return configInfo;
    }

}
