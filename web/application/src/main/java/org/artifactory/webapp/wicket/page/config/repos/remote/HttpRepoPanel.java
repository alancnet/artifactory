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

package org.artifactory.webapp.wicket.page.config.repos.remote;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.artifactory.addon.wicket.GemsWebAddon;
import org.artifactory.addon.wicket.NuGetWebAddon;
import org.artifactory.addon.wicket.PropertiesWebAddon;
import org.artifactory.addon.wicket.ReplicationWebAddon;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.util.HttpClientConfigurator;
import org.artifactory.util.HttpClientUtils;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.PathUtils;
import org.artifactory.webapp.wicket.page.config.repos.CachingDescriptorHelper;
import org.artifactory.webapp.wicket.page.config.repos.RepoConfigCreateUpdatePanel;
import org.artifactory.webapp.wicket.panel.tabbed.tab.BaseTab;
import org.artifactory.webapp.wicket.util.CronUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Remote repository configuration panel.
 *
 * @author Yossi Shaul
 */
public class HttpRepoPanel extends RepoConfigCreateUpdatePanel<HttpRepoDescriptor> {
    private static final Logger log = LoggerFactory.getLogger(HttpRepoPanel.class);

    private RemoteReplicationDescriptor replicationDescriptor;

    public HttpRepoPanel(CreateUpdateAction action, HttpRepoDescriptor repoDescriptor,
            CachingDescriptorHelper cachingDescriptorHelper) {
        super(action, repoDescriptor, cachingDescriptorHelper);
        setWidth(770);
    }

    @Override
    protected List<ITab> getConfigurationTabs() {
        List<ITab> tabs = Lists.newArrayList();

        tabs.add(new AbstractTab(Model.of("Basic Settings")) {
            @Override
            public Panel getPanel(String panelId) {
                return new HttpRepoBasicPanel(panelId, entity);
            }
        });

        tabs.add(new BaseTab(Model.of("Advanced Settings")) {
            @Override
            public Panel getPanel(String panelId) {
                return new HttpRepoAdvancedPanel(panelId, action, entity,
                        cachingDescriptorHelper.getModelMutableDescriptor());
            }

            @Override
            public void onNewTabLink(Component link) {
                super.onNewTabLink(link);
                link.add(new CssClass("wide-tab"));
            }
        });

        List<PropertySet> propertySets = getCachingDescriptorHelper().getModelMutableDescriptor().getPropertySets();
        PropertiesWebAddon propertiesWebAddon = addons.addonByType(PropertiesWebAddon.class);
        tabs.add(propertiesWebAddon.getRepoConfigPropertySetsTab("Property Sets", entity, propertySets));

        replicationDescriptor = cachingDescriptorHelper.getModelMutableDescriptor().getRemoteReplication(
                entity.getKey());
        if (replicationDescriptor == null) {
            replicationDescriptor = new RemoteReplicationDescriptor();
            replicationDescriptor.setRepoKey(entity.getKey());
        }
        ReplicationWebAddon replicationWebAddon = addons.addonByType(ReplicationWebAddon.class);
        tabs.add(replicationWebAddon.getHttpRepoReplicationPanel("Replication", entity, replicationDescriptor, action));

        // packages tab contains add-ons configuration
        tabs.add(new AbstractTab(Model.of("Packages")) {
            @Override
            public Panel getPanel(String panelId) {
                return new HttpRepoPackagesPanel<>(panelId, entity, isCreate());
            }
        });
        return tabs;
    }

    @Override
    public void addAndSaveDescriptor(HttpRepoDescriptor repoDescriptor) {
        CachingDescriptorHelper helper = getCachingDescriptorHelper();
        MutableCentralConfigDescriptor mccd = helper.getModelMutableDescriptor();
        if (StringUtils.isBlank(repoDescriptor.getP2OriginalUrl())) {
            repoDescriptor.setP2OriginalUrl(getUrlWithoutSubpath(repoDescriptor.getUrl()));
        }
        mccd.addRemoteRepository(repoDescriptor);
        if (replicationDescriptor.isEnabled()) {
            if (StringUtils.isBlank(replicationDescriptor.getRepoKey())) {
                replicationDescriptor.setRepoKey(repoDescriptor.getKey());
            }
            mccd.addRemoteReplication(replicationDescriptor);
        }
        helper.syncAndSaveRemoteRepositories();
    }

    @Override
    public void saveEditDescriptor(HttpRepoDescriptor repoDescriptor) {
        CachingDescriptorHelper helper = getCachingDescriptorHelper();
        MutableCentralConfigDescriptor mccd = helper.getModelMutableDescriptor();
        //update the model being saved
        Map<String, RemoteRepoDescriptor> remoteRepositoriesMap = mccd.getRemoteRepositoriesMap();
        if (remoteRepositoriesMap.containsKey(repoDescriptor.getKey())) {
            if (StringUtils.isBlank(repoDescriptor.getP2OriginalUrl())) {
                RemoteRepoDescriptor oldDescriptor = helper.getSavedMutableDescriptor().getRemoteRepositoriesMap().get(
                        repoDescriptor.getKey());
                if (oldDescriptor != null) {
                    repoDescriptor.setP2OriginalUrl(getUrlWithoutSubpath(oldDescriptor.getUrl()));
                }
            }
            remoteRepositoriesMap.put(repoDescriptor.getKey(), repoDescriptor);
        }
        if (replicationDescriptor.isEnabled() && !mccd.isRemoteReplicationExists(replicationDescriptor)) {
            if (StringUtils.isBlank(replicationDescriptor.getRepoKey())) {
                replicationDescriptor.setRepoKey(repoDescriptor.getKey());
            }
            mccd.addRemoteReplication(replicationDescriptor);
        }
        helper.syncAndSaveRemoteRepositories();
    }

    private String getUrlWithoutSubpath(String url) {
        int slashslash = url.indexOf("//") + 2;
        int nextSlash = url.indexOf('/', slashslash);
        return nextSlash < 0 ? url : PathUtils.trimSlashes(url.substring(0, nextSlash)).toString();
    }

    @Override
    protected boolean validate(HttpRepoDescriptor repoDescriptor) {
        boolean urlValid = StringUtils.isNotEmpty(repoDescriptor.getUrl());
        if (!urlValid) {
            error("Field 'Url' is required.");
            return false;
        }

        if (repoDescriptor.getSocketTimeoutMillis() < 0) {
            error("Socket Timeout must be positive or zero.");
            return false;
        }

        if (replicationDescriptor != null && replicationDescriptor.isEnabled()) {
            String cronExp = replicationDescriptor.getCronExp();
            if (StringUtils.isBlank(cronExp) || !CronUtils.isValid(cronExp)) {
                error("Invalid cron expression");
                return false;
            }
        }

        repoDescriptor.setPassword(CryptoHelper.encryptIfNeeded(repoDescriptor.getPassword()));

        return true;
    }

    @Override
    protected TitledAjaxSubmitLink createTestButton() {
        return new TitledAjaxSubmitLink("test", "Test", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                HttpRepoDescriptor repo = getRepoDescriptor();
                if (!validate(repo)) {
                    AjaxUtils.refreshFeedback();
                    return;
                }
                // always test with url trailing slash
                String url = PathUtils.addTrailingSlash(repo.getUrl());
                HttpRequestBase testMethod = getRemoteRepoTestMethod(url);
                CloseableHttpClient client = getRemoteRepoTestHttpClient(repo, url);
                CloseableHttpResponse response = null;
                try {
                    response = client.execute(testMethod);
                    boolean success = remoteRepoTestValidStatus(response.getStatusLine().getStatusCode());
                    if (!success) {
                        IOUtils.closeQuietly(response);
                        final Header serverHeader = response.getFirstHeader("Server");
                        // S3 hosted repositories are not hierarchical and does not have a notion of "collection" (folder, directory)
                        // Therefore we should not add the trailing slash when testing them
                        if (serverHeader != null && "AmazonS3".equals(serverHeader.getValue())) {
                            log.debug("Remote repository is hosted on Amazon S3, trying without a trailing slash");

                            url = repo.getUrl();
                            testMethod = getRemoteRepoTestMethod(url);
                            response = client.execute(testMethod);
                            success = remoteRepoTestValidStatus(response.getStatusLine().getStatusCode());
                        }
                    }
                    if (!success) {
                        error("Connection failed: Error " + response.getStatusLine().getStatusCode() + ": " +
                                response.getStatusLine().getReasonPhrase());
                    } else {
                        info("Successfully connected to server");
                    }
                } catch (Exception e) {
                    error("Connection failed with exception: " + HttpClientUtils.getErrorMessage(e));
                    log.debug("Test connection to '" + url + "' failed with exception", e);
                } finally {
                    IOUtils.closeQuietly(response);
                    IOUtils.closeQuietly(client);
                }
                AjaxUtils.refreshFeedback(target);
            }
        };
    }

    protected HttpRequestBase getRemoteRepoTestMethod(String url) {
        HttpRequestBase testMethod = null;
        HttpRepoDescriptor repo = getRepoDescriptor();

        if (repo.getType().equals(RepoType.Gems)) {
            GemsWebAddon gemsAddon = addons.addonByType(GemsWebAddon.class);
            testMethod = gemsAddon.getRemoteRepoTestMethod(url, repo);
        }
        if (testMethod == null) {
            testMethod = addons.addonByType(NuGetWebAddon.class).getRemoteRepoTestMethod(url, repo);
        }
        if (testMethod == null) {
            testMethod = new HttpHead(HttpUtils.encodeQuery(url));
        }
        return testMethod;
    }

    protected CloseableHttpClient getRemoteRepoTestHttpClient(HttpRepoDescriptor repo, String url) {
        return new HttpClientConfigurator()
                .hostFromUrl(url)
                .connectionTimeout(repo.getSocketTimeoutMillis())
                .soTimeout(repo.getSocketTimeoutMillis())
                .staleCheckingEnabled(true)
                .retry(1, false)
                .localAddress(repo.getLocalAddress())
                .proxy(repo.getProxy())
                .authentication(repo.getUsername(), CryptoHelper.decryptIfNeeded(repo.getPassword()),
                        repo.isAllowAnyHostAuth())
                .enableCookieManagement(repo.isEnableCookieManagement())
                .getClient();
    }

    protected boolean remoteRepoTestValidStatus(int status) {
        return status == HttpServletResponse.SC_OK || status == HttpServletResponse.SC_NO_CONTENT;
    }
}
