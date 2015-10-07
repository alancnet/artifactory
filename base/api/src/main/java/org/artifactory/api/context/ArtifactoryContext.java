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

package org.artifactory.api.context;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.config.ImportableExportable;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converters.ConverterManager;
import org.artifactory.converters.VersionProvider;
import org.artifactory.spring.SpringConfigPaths;

import java.util.Map;

/**
 * @author yoavl
 */
public interface ArtifactoryContext extends ImportableExportable {
    String MBEANS_DOMAIN_NAME = "org.jfrog.artifactory:";
    String APPLICATION_CONTEXT_KEY = "org.artifactory.spring.ApplicationContext";

    CentralConfigService getCentralConfig();

    public <T> T beanForType(Class<T> type);

    <T> T beanForType(String name, Class<T> type);

    public <T> Map<String, T> beansForType(Class<T> type);

    public Object getBean(String name);

    RepositoryService getRepositoryService();

    AuthorizationService getAuthorizationService();

    long getUptime();

    ArtifactoryHome getArtifactoryHome();

    String getContextId();

    SpringConfigPaths getConfigPaths();

    String getServerId();

    boolean isOffline();

    void setOffline();

    ConverterManager getConverterManager();

    VersionProvider getVersionProvider();

    void destroy();
}
