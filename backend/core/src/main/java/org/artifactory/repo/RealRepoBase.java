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

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.exception.BlackedOutException;
import org.artifactory.api.repo.exception.IncludeExcludeException;
import org.artifactory.api.repo.exception.SnapshotPolicyException;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.security.AccessLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class RealRepoBase<T extends RealRepoDescriptor> extends RepoBase<T> implements RealRepo<T> {
    private static final Logger log = LoggerFactory.getLogger(RealRepoBase.class);

    public RealRepoBase(T descriptor, InternalRepositoryService repositoryService) {
        super(descriptor, repositoryService);
    }

    @Override
    public boolean isHandleReleases() {
        return getDescriptor().isHandleReleases();
    }

    @Override
    public boolean isHandleSnapshots() {
        return getDescriptor().isHandleSnapshots();
    }

    @Override
    public boolean isBlackedOut() {
        return getDescriptor().isBlackedOut();
    }

    @Override
    public int getMaxUniqueSnapshots() {
        return getDescriptor().getMaxUniqueSnapshots();
    }

    @Override
    public boolean handlesReleaseSnapshot(String path) {
        if (NamingUtils.isMetadata(path) || NamingUtils.isChecksum(path) || NamingUtils.isSystem(path)) {
            return true;
        }
        ModuleInfo moduleInfo = getItemModuleInfo(path);
        if (!moduleInfo.isValid()) {
            log.debug("{} is not a valid module info -  '{}': not enforcing snapshot/release policy.", this, path);
            return true;
        }
        boolean snapshot = moduleInfo.isIntegration() || StringUtils.contains(path, "[INTEGRATION]");
        if (snapshot && !isHandleSnapshots()) {
            log.debug("{} rejected '{}': not handling snapshots.", this, path);
            return false;
        } else if (!snapshot && !isHandleReleases()) {
            log.debug("{} rejected '{}': not handling releases.", this, path);
            return false;
        }
        return true;
    }

    @Override
    public boolean isLocal() {
        return getDescriptor().isLocal();
    }

    @Override
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public BasicStatusHolder assertValidPath(RepoPath repoPath, boolean downloadRequest) {
        String path = repoPath.getPath();
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        statusHolder.setActivateLogging(log.isDebugEnabled());
        if (isBlackedOut()) {
            BlackedOutException exception = new BlackedOutException(this.getDescriptor(), getRepoPath(path));
            statusHolder.error(exception.getMessage(), exception.getErrorCode(), exception, log);
        } else if (!handlesReleaseSnapshot(path)) {
            SnapshotPolicyException exception = new SnapshotPolicyException(this.getDescriptor(), getRepoPath(path));
            statusHolder.error(exception.getMessage(), exception.getErrorCode(), exception, log);
        } else if (!accepts(repoPath)) {
            IncludeExcludeException exception = new IncludeExcludeException(
                    downloadRequest ? HttpStatus.SC_NOT_FOUND : HttpStatus.SC_CONFLICT, this.getDescriptor(),
                    getRepoPath(path));
            statusHolder.error(exception.getMessage(), exception.getErrorCode(), exception, log);
        }
        return statusHolder;
    }

    protected void assertReadPermissions(RepoPath repoPath, MutableStatusHolder status) {
        AuthorizationService authService = getAuthorizationService();
        boolean canRead = authService.canRead(repoPath);
        if (!canRead) {
            status.error("Download request for repo:path '" + repoPath + "' is forbidden for user '" +
                    authService.currentUsername() + "'.", HttpStatus.SC_FORBIDDEN, log);
            AccessLogger.downloadDenied(repoPath);
        }
    }

}