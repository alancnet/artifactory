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

package org.artifactory.repo.mbean;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.storage.fs.service.FileService;

/**
 * An MBean to expose repository data.
 *
 * @author Yossi Shaul
 */
public class ManagedRepository implements ManagedRepositoryMBean {
    private final LocalRepoDescriptor descriptor;
    private final FileService fileService;

    public ManagedRepository(LocalRepoDescriptor descriptor) {
        this.descriptor = descriptor;
        fileService = ContextHelper.get().beanForType(FileService.class);
    }

    @Override
    public String getRepositoryKey() {
        return descriptor.getKey();
    }

    @Override
    public long getArtifactsCount() {
        return fileService.getFilesCount(new RepoPathImpl(descriptor.getKey(), ""));
    }

    @Override
    public long getArtifactsTotalSize() {
        return fileService.getFilesTotalSize(new RepoPathImpl(descriptor.getKey(), ""));
    }
}
