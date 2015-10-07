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

package org.artifactory.repo;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.delegation.ContentSynchronisation;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.repo.BowerConfiguration;
import org.artifactory.descriptor.repo.ChecksumPolicyType;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.VcsConfiguration;
import org.artifactory.descriptor.repo.vcs.VcsGitProvider;
import org.artifactory.descriptor.repo.vcs.VcsType;
import org.artifactory.util.RepoLayoutUtils;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Remote Repository configuration
 *
 * @author Tomer Cohen
 * @see org.artifactory.descriptor.repo.HttpRepoDescriptor
 */
public class HttpRepositoryConfigurationImpl extends RepositoryConfigurationBase
        implements HttpRepositoryConfiguration {

    private String url;
    private String username = "";
    private String password = "";
    private String proxy;
    private boolean handleReleases = true;
    private boolean handleSnapshots = true;
    private boolean suppressPomConsistencyChecks = false;
    private String remoteRepoChecksumPolicyType = "";
    private boolean hardFail = false;
    private boolean offline = false;
    private boolean blackedOut = false;
    private boolean storeArtifactsLocally = true;
    private int socketTimeoutMillis = 15000;
    private String localAddress = "";
    private long retrievalCachePeriodSecs = 600L;
    private long assumedOfflinePeriodSecs = 300L;
    private long missedRetrievalCachePeriodSecs = 1800L;
    private int unusedArtifactsCleanupPeriodHours = 0;
    private boolean fetchJarsEagerly = false;
    private boolean fetchSourcesEagerly = false;
    private boolean shareConfiguration = false;
    private boolean synchronizeProperties = false;
    private int maxUniqueSnapshots = 0;
    private List<String> propertySets;
    private String remoteRepoLayoutRef;
    private boolean archiveBrowsingEnabled = false;
    private boolean listRemoteFolderItems = true;
    private boolean rejectInvalidJars = false;
    private boolean allowAnyHostAuth;
    private boolean enableCookieManagement;
    private boolean enableTokenAuthentication;
    private String queryParams;
    private BowerConfiguration bowerConfiguration = new BowerConfiguration();
    private VcsConfiguration vcsConfiguration = new VcsConfiguration();
    private ContentSynchronisation contentSynchronisation;

    public HttpRepositoryConfigurationImpl() {
        setRepoLayoutRef(RepoLayoutUtils.MAVEN_2_DEFAULT_NAME);
    }

    public HttpRepositoryConfigurationImpl(HttpRepoDescriptor repoDescriptor) {
        super(repoDescriptor, TYPE);
        try {
            new URL(repoDescriptor.getUrl());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Remote URL '"+repoDescriptor.getUrl()+"' is not valid", e);
        }
        this.url = repoDescriptor.getUrl();
        String username = repoDescriptor.getUsername();
        if (StringUtils.isNotBlank(username)) {
            setUsername(username);
        }
        String password = repoDescriptor.getPassword();
        if (StringUtils.isNotBlank(password)) {
            setPassword(password);
        }
        setAllowAnyHostAuth(repoDescriptor.isAllowAnyHostAuth());
        setEnableTokenAuthentication(repoDescriptor.isEnableTokenAuthentication());
        ProxyDescriptor proxy = repoDescriptor.getProxy();
        if (proxy != null) {
            setProxy(proxy.getKey());
        }
        Map<String, String> checksumPolicyTypes = extractXmlValueFromEnumAnnotations(ChecksumPolicyType.class);
        for (Map.Entry<String, String> checksumPolicyType : checksumPolicyTypes.entrySet()) {
            if (checksumPolicyType.getKey().equals(repoDescriptor.getChecksumPolicyType().name())) {
                setRemoteRepoChecksumPolicyType(checksumPolicyType.getKey());
            }
        }
        setHardFail(repoDescriptor.isHardFail());
        setOffline(repoDescriptor.isOffline());
        setStoreArtifactsLocally(repoDescriptor.isStoreArtifactsLocally());
        setSocketTimeoutMillis(repoDescriptor.getSocketTimeoutMillis());
        String localAddress = repoDescriptor.getLocalAddress();
        if (StringUtils.isNotBlank(localAddress)) {
            setLocalAddress(localAddress);
        }
        setEnableCookieManagement(repoDescriptor.isEnableCookieManagement());
        setBlackedOut(repoDescriptor.isBlackedOut());
        setHandleReleases(repoDescriptor.isHandleReleases());
        setHandleSnapshots(repoDescriptor.isHandleSnapshots());
        setSuppressPomConsistencyChecks(repoDescriptor.isSuppressPomConsistencyChecks());
        setRetrievalCachePeriodSecs(repoDescriptor.getRetrievalCachePeriodSecs());
        setAssumedOfflinePeriodSecs(repoDescriptor.getAssumedOfflinePeriodSecs());
        setMissedRetrievalCachePeriodSecs(repoDescriptor.getMissedRetrievalCachePeriodSecs());
        setUnusedArtifactsCleanupPeriodHours(repoDescriptor.getUnusedArtifactsCleanupPeriodHours());
        setFetchJarsEagerly(repoDescriptor.isFetchJarsEagerly());
        setFetchSourcesEagerly(repoDescriptor.isFetchSourcesEagerly());
        setShareConfiguration(repoDescriptor.isShareConfiguration());
        setMaxUniqueSnapshots(repoDescriptor.getMaxUniqueSnapshots());
        setSynchronizeProperties(repoDescriptor.isSynchronizeProperties());
        setContentSynchronisation(repoDescriptor.getContentSynchronisation());
        List<PropertySet> propertySets = repoDescriptor.getPropertySets();
        if (propertySets != null && !propertySets.isEmpty()) {
            setPropertySets(Lists.transform(propertySets, new Function<PropertySet, String>() {
                @Override
                public String apply(@Nonnull PropertySet input) {
                    return input.getName();
                }
            }));
        } else {
            setPropertySets(Lists.<String>newArrayList());
        }
        RepoLayout remoteRepoLayout = repoDescriptor.getRemoteRepoLayout();
        if (remoteRepoLayout != null) {
            setRemoteRepoLayoutRef(remoteRepoLayout.getName());
        }
        setArchiveBrowsingEnabled(repoDescriptor.isArchiveBrowsingEnabled());
        setListRemoteFolderItems(repoDescriptor.isListRemoteFolderItems());
        setRejectInvalidJars(repoDescriptor.isRejectInvalidJars());
        this.bowerConfiguration = repoDescriptor.getBower();
        this.vcsConfiguration = repoDescriptor.getVcs();
    }

    @Override
    public int getMaxUniqueSnapshots() {
        return maxUniqueSnapshots;
    }

    public void setMaxUniqueSnapshots(int maxUniqueSnapshots) {
        this.maxUniqueSnapshots = maxUniqueSnapshots;
    }

    @Override
    public boolean isSuppressPomConsistencyChecks() {
        return suppressPomConsistencyChecks;
    }

    public void setSuppressPomConsistencyChecks(boolean suppressPomConsistencyChecks) {
        this.suppressPomConsistencyChecks = suppressPomConsistencyChecks;
    }

    @Override
    public boolean isHandleReleases() {
        return handleReleases;
    }

    public void setHandleReleases(boolean handleReleases) {
        this.handleReleases = handleReleases;
    }

    @Override
    public boolean isHandleSnapshots() {
        return handleSnapshots;
    }

    public void setHandleSnapshots(boolean handleSnapshots) {
        this.handleSnapshots = handleSnapshots;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean isBlackedOut() {
        return blackedOut;
    }

    public void setBlackedOut(boolean blackedOut) {
        this.blackedOut = blackedOut;
    }

    @Override
    public long getAssumedOfflinePeriodSecs() {
        return assumedOfflinePeriodSecs;
    }

    public void setAssumedOfflinePeriodSecs(long assumedOfflinePeriodSecs) {
        this.assumedOfflinePeriodSecs = assumedOfflinePeriodSecs;
    }

    @Override
    public boolean isFetchJarsEagerly() {
        return fetchJarsEagerly;
    }

    public void setFetchJarsEagerly(boolean fetchJarsEagerly) {
        this.fetchJarsEagerly = fetchJarsEagerly;
    }

    @Override
    public boolean isFetchSourcesEagerly() {
        return fetchSourcesEagerly;
    }

    public void setFetchSourcesEagerly(boolean fetchSourcesEagerly) {
        this.fetchSourcesEagerly = fetchSourcesEagerly;
    }

    @Override
    public boolean isHardFail() {
        return hardFail;
    }

    public void setHardFail(boolean hardFail) {
        this.hardFail = hardFail;
    }

    @Override
    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    @Override
    public long getMissedRetrievalCachePeriodSecs() {
        return missedRetrievalCachePeriodSecs;
    }

    public void setMissedRetrievalCachePeriodSecs(long missedRetrievalCachePeriodSecs) {
        this.missedRetrievalCachePeriodSecs = missedRetrievalCachePeriodSecs;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public List<String> getPropertySets() {
        return propertySets;
    }

    public void setPropertySets(List<String> propertySets) {
        this.propertySets = propertySets;
    }

    @Override
    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    @Override
    public String getRemoteRepoChecksumPolicyType() {
        return remoteRepoChecksumPolicyType;
    }

    public void setRemoteRepoChecksumPolicyType(String remoteRepoChecksumPolicyType) {
        this.remoteRepoChecksumPolicyType = remoteRepoChecksumPolicyType;
    }

    @Override
    public long getRetrievalCachePeriodSecs() {
        return retrievalCachePeriodSecs;
    }

    public void setRetrievalCachePeriodSecs(long retrievalCachePeriodSecs) {
        this.retrievalCachePeriodSecs = retrievalCachePeriodSecs;
    }

    @Override
    public boolean isShareConfiguration() {
        return shareConfiguration;
    }

    public void setShareConfiguration(boolean shareConfiguration) {
        this.shareConfiguration = shareConfiguration;
    }

    @Override
    public int getSocketTimeoutMillis() {
        return socketTimeoutMillis;
    }

    public void setSocketTimeoutMillis(int socketTimeoutMillis) {
        this.socketTimeoutMillis = socketTimeoutMillis;
    }

    @Override
    public boolean isStoreArtifactsLocally() {
        return storeArtifactsLocally;
    }

    public void setStoreArtifactsLocally(boolean storeArtifactsLocally) {
        this.storeArtifactsLocally = storeArtifactsLocally;
    }

    @Override
    public boolean isSynchronizeProperties() {
        return synchronizeProperties;
    }

    public void setSynchronizeProperties(boolean synchronizeProperties) {
        this.synchronizeProperties = synchronizeProperties;
    }

    @Override
    public int getUnusedArtifactsCleanupPeriodHours() {
        return unusedArtifactsCleanupPeriodHours;
    }

    public void setUnusedArtifactsCleanupPeriodHours(int unusedArtifactsCleanupPeriodHours) {
        this.unusedArtifactsCleanupPeriodHours = unusedArtifactsCleanupPeriodHours;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getRemoteRepoLayoutRef() {
        return remoteRepoLayoutRef;
    }

    public void setRemoteRepoLayoutRef(String remoteRepoLayoutRef) {
        this.remoteRepoLayoutRef = remoteRepoLayoutRef;
    }

    @Override
    public boolean isArchiveBrowsingEnabled() {
        return archiveBrowsingEnabled;
    }

    public void setArchiveBrowsingEnabled(boolean archiveBrowsingEnabled) {
        this.archiveBrowsingEnabled = archiveBrowsingEnabled;
    }

    @Override
    public boolean isListRemoteFolderItems() {
        return listRemoteFolderItems;
    }

    public void setListRemoteFolderItems(boolean listRemoteFolderItems) {
        this.listRemoteFolderItems = listRemoteFolderItems;
    }

    @Override
    public boolean isRejectInvalidJars() {
        return rejectInvalidJars;
    }

    public void setRejectInvalidJars(boolean rejectInvalidJars) {
        this.rejectInvalidJars = rejectInvalidJars;
    }

    @Override
    public boolean isAllowAnyHostAuth() {
        return allowAnyHostAuth;
    }

    public void setAllowAnyHostAuth(boolean allowAnyHostAuth) {
        this.allowAnyHostAuth = allowAnyHostAuth;
    }

    @Override
    public boolean isEnableTokenAuthentication() {
        return enableTokenAuthentication;
    }

    public void setEnableTokenAuthentication(boolean enableTokenAuthentication) {
        this.enableTokenAuthentication = enableTokenAuthentication;
    }

    @Override
    public String getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    @Override
    public boolean isEnableCookieManagement() {
        return enableCookieManagement;
    }

    public void setEnableCookieManagement(boolean enableCookieManagement) {
        this.enableCookieManagement = enableCookieManagement;
    }

    @Override
    public String getBowerRegistryUrl() {
        return this.bowerConfiguration != null ? this.bowerConfiguration.getBowerRegistryUrl() : null;
    }

    @Override
    public String getVcsType() {
        return this.vcsConfiguration != null ? this.vcsConfiguration.getType().name() : null;
    }

    @Override
    public String getVcsGitProvider() {
        return this.vcsConfiguration != null ? this.vcsConfiguration.getGit().getProvider().name() : null;
    }

    @Override
    public String getVcsGitDownloadUrl() {
        return this.vcsConfiguration != null ? this.vcsConfiguration.getGit().getDownloadUrl() : null;
    }

    // Keep these setters for jackson
    public void setBowerRegistryUrl(String bowerRegistryUrl) {
        this.bowerConfiguration.setBowerRegistryUrl(bowerRegistryUrl);
    }

    public void setVcsType(String vcsType) {
        this.vcsConfiguration.setType(VcsType.valueOf(vcsType));
    }

    public void setVcsGitProvider(String gitProvider) {
        this.vcsConfiguration.getGit().setProvider(VcsGitProvider.valueOf(gitProvider));
    }

    public void setVcsGitDownloadUrl(String vcsGitDownloadUrl) {
        this.vcsConfiguration.getGit().setDownloadUrl(vcsGitDownloadUrl);
    }

    public ContentSynchronisation getContentSynchronisation() {
        return contentSynchronisation;
    }

    public void setContentSynchronisation(ContentSynchronisation contentSynchronisation) {
        this.contentSynchronisation = contentSynchronisation;
    }
}
