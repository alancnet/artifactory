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

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converters.ConverterManager;
import org.artifactory.converters.VersionProvider;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.spring.SpringConfigPaths;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mamo
 */
public class DummyArtifactoryContext extends ClassPathXmlApplicationContext implements ArtifactoryContext {
    private ApplicationContext applicationContext;

    public DummyArtifactoryContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public CentralConfigService getCentralConfig() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T beanForType(Class<T> type) {
        return applicationContext.getBean(type);
    }

    @Override
    public <T> T beanForType(String name, Class<T> type) {
        return null;
    }

    @Override
    public <T> Map<String, T> beansForType(Class<T> type) {
        return new HashMap<>();
    }

    @Override
    public Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    @Override
    public RepositoryService getRepositoryService() {
        return null;
    }

    @Override
    public AuthorizationService getAuthorizationService() {
        return (AuthorizationService) applicationContext.getBean("authorizationService");
    }

    @Override
    public long getUptime() {
        return 0;
    }

    @Override
    public ArtifactoryHome getArtifactoryHome() {
        return ArtifactoryHome.get();
    }

    @Override
    public String getContextId() {
        return null;
    }

    @Override
    public SpringConfigPaths getConfigPaths() {
        return null;
    }

    @Override
    public String getServerId() {
        return null;
    }

    @Override
    public boolean isOffline() {
        return false;
    }

    @Override
    public void setOffline() {
    }

    @Override
    public ConverterManager getConverterManager() {
        return null;
    }

    @Override
    public VersionProvider getVersionProvider() {
        return null;
    }

    @Override
    public void exportTo(ExportSettings settings) {
    }

    @Override
    public void importFrom(ImportSettings settings) {
    }
}
