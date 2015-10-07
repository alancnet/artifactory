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

import java.util.List;

/**
 * @author Yoav Landman
 */
public interface HttpRepositoryConfiguration extends RepositoryConfiguration {
    String TYPE = "remote";

    int getMaxUniqueSnapshots();

    boolean isSuppressPomConsistencyChecks();

    boolean isHandleReleases();

    boolean isHandleSnapshots();

    String getUrl();

    boolean isBlackedOut();

    long getAssumedOfflinePeriodSecs();

    boolean isFetchJarsEagerly();

    boolean isFetchSourcesEagerly();

    boolean isHardFail();

    String getLocalAddress();

    long getMissedRetrievalCachePeriodSecs();

    boolean isOffline();

    String getPassword();

    List<String> getPropertySets();

    String getProxy();

    String getRemoteRepoChecksumPolicyType();

    long getRetrievalCachePeriodSecs();

    boolean isShareConfiguration();

    int getSocketTimeoutMillis();

    boolean isStoreArtifactsLocally();

    boolean isSynchronizeProperties();

    int getUnusedArtifactsCleanupPeriodHours();

    String getUsername();

    String getRemoteRepoLayoutRef();

    boolean isArchiveBrowsingEnabled();

    boolean isListRemoteFolderItems();

    boolean isRejectInvalidJars();

    boolean isAllowAnyHostAuth();

    boolean isEnableCookieManagement();

    boolean isEnableTokenAuthentication();

    String getQueryParams();

    String getBowerRegistryUrl();

    String getVcsType();

    String getVcsGitProvider();

    String getVcsGitDownloadUrl();
}
