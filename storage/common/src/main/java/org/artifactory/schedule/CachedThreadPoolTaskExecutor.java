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

package org.artifactory.schedule;


import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import java.util.concurrent.Future;

/**
 * @author Yoav Landman
 */
public class CachedThreadPoolTaskExecutor extends ConcurrentTaskExecutor {

    public CachedThreadPoolTaskExecutor() {
        super(new ArtifactoryConcurrentExecutor());
    }

    private ArtifactoryConcurrentExecutor getArtifactoryExecutor() {
        return (ArtifactoryConcurrentExecutor) getConcurrentExecutor();
    }

    public void destroy() {
        getArtifactoryExecutor().shutdown();
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return getArtifactoryExecutor().submit(task, result);
    }
}
