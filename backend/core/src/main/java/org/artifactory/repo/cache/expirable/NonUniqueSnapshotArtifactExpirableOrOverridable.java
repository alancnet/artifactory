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

package org.artifactory.repo.cache.expirable;

import org.artifactory.mime.MavenNaming;
import org.artifactory.repo.LocalCacheRepo;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.local.LocalNonCacheOverridable;
import org.springframework.stereotype.Component;

/**
 * @author Noam Y. Tenne
 */
@Component
public class NonUniqueSnapshotArtifactExpirableOrOverridable implements CacheExpirable, LocalNonCacheOverridable {

    @Override
    public boolean isExpirable(LocalCacheRepo localCacheRepo, String path) {
        return isIntegrationAndNonUnique(localCacheRepo, path);
    }

    @Override
    public boolean isOverridable(LocalRepo repo, String path) {
        return isIntegrationAndNonUnique(repo, path);
    }

    private boolean isIntegrationAndNonUnique(LocalRepo repo, String path) {
        return repo.getItemModuleInfo(path).isIntegration() && !MavenNaming.isUniqueSnapshot(path);
    }
}
