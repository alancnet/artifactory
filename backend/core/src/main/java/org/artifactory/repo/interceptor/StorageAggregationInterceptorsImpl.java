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

package org.artifactory.repo.interceptor;

import org.artifactory.common.MutableStatusHolder;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.interceptor.StorageAggregationInterceptor;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.db.DbService;
import org.springframework.stereotype.Service;

/**
 * @author Noam Y. Tenne
 */
@Service
@Reloadable(beanClass = StorageAggregationInterceptors.class, initAfter = DbService.class)
public class StorageAggregationInterceptorsImpl extends Interceptors<StorageAggregationInterceptor>
        implements StorageAggregationInterceptors {

    @Override
    public void afterRepoImport(RepoPath rootRepoPath, int itemsCount, MutableStatusHolder status) {
        for (StorageAggregationInterceptor interceptor : this) {
            interceptor.afterRepoImport(rootRepoPath, itemsCount, status);
        }
    }
}
