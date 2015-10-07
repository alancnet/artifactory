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

package org.artifactory.repo.count;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.schedule.CachedThreadPoolTaskExecutor;
import org.artifactory.storage.fs.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

/**
 * Retrieves the total artifact (file) count and caching the result if the action takes too long.
 *
 * @author Shay Yaakov
 */
public class ArtifactCountRetriever {
    private static final Logger log = LoggerFactory.getLogger(ArtifactCountRetriever.class);

    private FileService fileService;
    private CachedThreadPoolTaskExecutor executor;
    private volatile int artifactCount = -1;
    private Semaphore dbFetchSemaphore = new Semaphore(1);

    public ArtifactCountRetriever() {
        ArtifactoryContext context = ContextHelper.get();
        this.fileService = context.beanForType(FileService.class);
        this.executor = context.beanForType(CachedThreadPoolTaskExecutor.class);
    }

    public int getCount() {
        if (artifactCount > -1) {
            submitAsyncFetch();
        } else {
            submitSyncFetch();
        }
        return artifactCount;
    }

    private void submitAsyncFetch() {
        if (!dbFetchSemaphore.tryAcquire()) {
            log.debug("Skipping artifact count from DB since the process is already running.");
            return;
        }

        try {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        artifactCount = fileService.getFilesCount();
                    } finally {
                        dbFetchSemaphore.release();
                    }
                }
            });
        } catch (Exception e) {
            dbFetchSemaphore.release();
            throw new RepositoryRuntimeException("Exception while async fetching total artifact count", e);
        }
    }

    private void submitSyncFetch() {
        try {
            dbFetchSemaphore.acquire();
            if (artifactCount < 0) {
                artifactCount = fileService.getFilesCount();
            }
        } catch (InterruptedException e) {
            log.debug("Caught interrupted exception from sync submit: {}", e);
        } finally {
            dbFetchSemaphore.release();
        }
    }
}