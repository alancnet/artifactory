/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.addon.blackduck;

import org.artifactory.addon.Addon;
import org.artifactory.api.governance.BlackDuckApplicationInfo;
import org.artifactory.api.governance.GovernanceRequestInfo;
import org.artifactory.api.repo.Async;
import org.artifactory.api.rest.compliance.FileComplianceInfo;
import org.artifactory.descriptor.external.BlackDuckSettingsDescriptor;
import org.artifactory.repo.RepoPath;
import org.jfrog.build.api.Build;

import java.util.Collection;
import java.util.Set;

/**
 * @author mamo
 */
public interface BlackDuckAddon extends Addon {

    String UNDEFINED_LICENSE = "undefined";

    /**
     * returns external component info as found in the metadata
     */
    FileComplianceInfo getExternalInfoFromMetadata(RepoPath repoPath);

    /**
     * Perform Black Duck application validation on given {@code build}
     *
     * @param build The build to perform the license calculation on.
     */
    @Async(delayUntilAfterCommit = true)
    void performBlackDuckOnBuildArtifacts(Build build);

    void testConnection(BlackDuckSettingsDescriptor blackDuckSettingsDescriptor) throws Exception;

    ExternalComponentInfo getBlackduckInfo(RepoPath repoPath);

    /**
     * Used by the UI to trigger Black Duck calculation on a single path
     */
    boolean queryCodeCenterForPath(RepoPath path);

    String getComponentExternalIdFromProperty(RepoPath repoPath);

    String getComponentIdFromProperty(RepoPath repoPath);

    void clearComponentIdProperty(RepoPath repoPath);

    void setComponentExternalIdProperty(RepoPath repoPath, String updatedComponentId);

    boolean isEnableIntegration();

    Collection<GovernanceRequestInfo> getGovernanceRequestInfos(Build build, String appName, String appVersion,
                                                                Set<String> scopes);

    BlackDuckApplicationInfo blackDuckApplicationInfo(String appInfo, String versionInfo);

    String updateRequest(Build build, GovernanceRequestInfo requestInfo);

    boolean isSupportedPackageType(RepoPath path);

    Set<String> getPathLicensesFromProperties(RepoPath path);

    String getLicenseUrl(String license);
}
