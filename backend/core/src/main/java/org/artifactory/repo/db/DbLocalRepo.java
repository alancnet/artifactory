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

package org.artifactory.repo.db;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.repo.exception.FileExpectedException;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.common.StatusHolder;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.SnapshotVersionBehavior;
import org.artifactory.fs.RepoResource;
import org.artifactory.io.NullResourceStreamHandle;
import org.artifactory.io.SimpleResourceStreamHandle;
import org.artifactory.io.checksum.policy.ChecksumPolicy;
import org.artifactory.io.checksum.policy.LocalRepoChecksumPolicy;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RealRepoBase;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.SaveResourceContext;
import org.artifactory.repo.db.importexport.DbRepoExportHandler;
import org.artifactory.repo.db.importexport.DbRepoImportHandler;
import org.artifactory.repo.local.PathDeletionContext;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.snapshot.MavenSnapshotVersionAdapter;
import org.artifactory.repo.snapshot.SnapshotVersionAdapterBase;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.request.RepoRequests;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.resource.UnfoundRepoResource;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.sapi.fs.MutableVfsFile;
import org.artifactory.sapi.fs.MutableVfsFolder;
import org.artifactory.sapi.fs.MutableVfsItem;
import org.artifactory.sapi.fs.VfsFile;
import org.artifactory.sapi.fs.VfsFolder;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.schedule.TaskCallback;
import org.artifactory.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

/**
 * A local repository implementation backed by db.
 *
 * @author Yossi Shaul
 */
public class DbLocalRepo<T extends LocalRepoDescriptor> extends RealRepoBase<T> implements LocalRepo<T> {
    private static final Logger log = LoggerFactory.getLogger(DbLocalRepo.class);

    protected final DbStoringRepoMixin<LocalRepoDescriptor> mixin;

    // For local non-cache repositories use the special local repo checksum policy
    private final LocalRepoChecksumPolicy checksumPolicy = new LocalRepoChecksumPolicy();

    private final MavenSnapshotVersionAdapter mavenSnapshotVersionAdapter;

    public DbLocalRepo(T descriptor, InternalRepositoryService repositoryService, DbLocalRepo<T> oldLocalRepo) {
        super(descriptor, repositoryService);
        DbStoringRepoMixin<LocalRepoDescriptor> oldMixin = oldLocalRepo != null ? oldLocalRepo.mixin : null;
        mixin = new DbStoringRepoMixin<LocalRepoDescriptor>(descriptor, oldMixin);
        checksumPolicy.setPolicyType(descriptor.getChecksumPolicyType());
        SnapshotVersionBehavior snapshotVersionBehavior = descriptor.getSnapshotVersionBehavior();
        mavenSnapshotVersionAdapter = SnapshotVersionAdapterBase.getByType(snapshotVersionBehavior);
    }

    @Override
    public void init() throws StorageException {
        mixin.init();
    }

    @Override
    public RepoResource getInfo(InternalRequestContext context) throws FileExpectedException {
        final String path = context.getResourcePath();
        RepoPath repoPath = InternalRepoPathFactory.create(getKey(), path);
        StatusHolder statusHolder = checkDownloadIsAllowed(repoPath);
        if (statusHolder.isError()) {
            RepoRequests.logToContext("Download denied (%s) - returning unfound resource", statusHolder.getStatusMsg());
            return new UnfoundRepoResource(repoPath, statusHolder.getStatusMsg(), statusHolder.getStatusCode());
        }
        RepoResource res = mixin.getInfo(context);
        checkAndMarkExpirableResource(res);
        return res;
    }

    @Override
    public ResourceStreamHandle getResourceStreamHandle(InternalRequestContext requestContext, RepoResource res)
            throws IOException {
        return mixin.getResourceStreamHandle(requestContext, res);
    }

    @Override
    public boolean itemExists(String relativePath) {
        return mixin.itemExists(relativePath);
    }

    @Override
    public String getKey() {
        return mixin.getKey();
    }

    @Override
    public boolean isWriteLocked(RepoPath repoPath) {
        return mixin.isWriteLocked(getRepoPath(repoPath.getPath()));
    }

    @Override
    public StatusHolder checkDownloadIsAllowed(RepoPath repoPath) {
        BasicStatusHolder status = assertValidPath(repoPath, true);
        if (status.isError()) {
            return status;
        }
        assertReadPermissions(repoPath, status);
        return status;
    }

    @Override
    public RepoResource saveResource(SaveResourceContext context) throws RepoRejectException, IOException {
        return mixin.saveResource(context);
    }

    @Override
    public boolean shouldProtectPathDeletion(PathDeletionContext pathDeletionContext) {
        return mixin.shouldProtectPathDeletion(pathDeletionContext);
    }

    @Override
    public void importFrom(ImportSettings settings) {
        DbRepoImportHandler importHandler = new DbRepoImportHandler(this, settings, TaskCallback.currentTaskToken());
        importHandler.executeImport();
        importHandler.finalizeImport();
    }


    @Override
    public void exportTo(ExportSettings settings) {
        new DbRepoExportHandler(this, settings).export();
    }

    @Override
    public String getTextFileContent(RepoPath repoPath) {
        VfsFile file = mixin.getImmutableFile(repoPath);
        if (file != null) {
            InputStream is = null;
            try {
                is = file.getStream();
                return IOUtils.toString(is, Charsets.UTF_8.name());
            } catch (IOException e) {
                throw new RuntimeException("Could not read text file content from '" + repoPath + "'.", e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        return "";
    }

    @Override
    public ResourceStreamHandle getFileContent(RepoPath repoPath) {
        VfsFile file = mixin.getImmutableFile(repoPath);
        if (file != null) {
            InputStream is = file.getStream();
            return new SimpleResourceStreamHandle(is, file.length());
        } else {
            return new NullResourceStreamHandle();
        }
    }

    @Override
    public void undeploy(RepoPath repoPath) {
        undeploy(repoPath, false);
    }

    @Override
    public void undeploy(RepoPath repoPath, boolean calcMavenMetadata) {
        mixin.undeploy(repoPath, calcMavenMetadata);
    }

    @Override
    public SnapshotVersionBehavior getMavenSnapshotVersionBehavior() {
        return getDescriptor().getSnapshotVersionBehavior();
    }

    @Override
    public MavenSnapshotVersionAdapter getMavenSnapshotVersionAdapter() {
        return mavenSnapshotVersionAdapter;
    }

    @Override
    public boolean isSuppressPomConsistencyChecks() {
        return getDescriptor().isSuppressPomConsistencyChecks();
    }

    @Override
    public ChecksumPolicy getChecksumPolicy() {
        return checksumPolicy;
    }


    @Override
    @Nullable
    public MutableVfsItem getMutableFsItem(RepoPath repoPath) {
        return mixin.getMutableItem(repoPath);
    }

    @Override
    @Nullable
    public MutableVfsFile getMutableFile(RepoPath repoPath) {
        return mixin.getMutableFile(repoPath);
    }

    @Override
    @Nonnull
    public MutableVfsFile createOrGetFile(RepoPath repoPath) {
        return mixin.createOrGetFile(repoPath);
    }

    @Override
    @Nullable
    public MutableVfsFolder getMutableFolder(RepoPath repoPath) {
        return mixin.getMutableFolder(repoPath);
    }

    @Override
    @Nonnull
    public MutableVfsFolder createOrGetFolder(RepoPath repoPath) {
        return mixin.createOrGetFolder(repoPath);
    }

    @Override
    @Nullable
    public VfsItem getImmutableFsItem(RepoPath repoPath) {
        return mixin.getImmutableItem(repoPath);
    }

    @Override
    @Nullable
    public VfsFile getImmutableFile(RepoPath repoPath) {
        return mixin.getImmutableFile(repoPath);
    }

    @Override
    public VfsFolder getImmutableFolder(RepoPath repoPath) {
        return mixin.getImmutableFolder(repoPath);
    }
}
