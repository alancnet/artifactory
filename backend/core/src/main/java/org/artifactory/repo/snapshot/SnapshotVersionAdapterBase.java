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

package org.artifactory.repo.snapshot;

import org.artifactory.descriptor.repo.SnapshotVersionBehavior;
import org.artifactory.mime.NamingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base abstract class for {@link MavenSnapshotVersionAdapter}s.
 *
 * @author Yossi Shaul
 */
public abstract class SnapshotVersionAdapterBase implements MavenSnapshotVersionAdapter {
    private static final Logger log = LoggerFactory.getLogger(SnapshotVersionAdapterBase.class);

    public static MavenSnapshotVersionAdapter getByType(SnapshotVersionBehavior type) {
        switch (type) {
            case DEPLOYER:
                return new DeployerSnapshotVersionAdapter();
            case UNIQUE:
                return new UniqueSnapshotVersionAdapter();
            case NONUNIQUE:
                return new NonUniqueSnapshotVersionAdapter();
            default:
                throw new IllegalArgumentException("No snapshot version adapter found for type " + type);
        }
    }

    @Override
    public final String adaptSnapshotPath(MavenSnapshotVersionAdapterContext context) {
        String path = context.getRepoPath().getPath();
        if (!isApplicableOn(context)) {
            return path;
        }

        return adapt(context);
    }

    protected String adapt(MavenSnapshotVersionAdapterContext context) {
        return context.getRepoPath().getPath();
    }

    /**
     * Shared method for inheriting adapters to determine if a certain path is eligible for path adapters.
     *
     * @param context
     */
    protected boolean isApplicableOn(MavenSnapshotVersionAdapterContext context) {
        // don't modify metadata paths
        String path = context.getRepoPath().getPath();
        boolean metadataArtifact = NamingUtils.isMetadata(path) || NamingUtils.isMetadataChecksum(path);

        if (metadataArtifact) {
            log.debug("Not applying snapshot policy on metadata path: {}", path);
            return false;
        }

        // don't modify files that are not snapshots according to the file name (RTFACT-3049)
        if (!context.getModuleInfo().isIntegration()) {
            log.debug("Not applying snapshot policy on non snapshot file: {}", path);
            return false;
        }

        if (!context.getModuleInfo().isValid()) {
            log.debug("{} is not a valid maven GAV path. Not applying snapshot policy.", path);
            return false;
        }

        return true;
    }
}