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

package org.artifactory.io.checksum;

import com.google.common.collect.ImmutableCollection;
import org.artifactory.spring.ReloadableBean;
import org.artifactory.storage.binstore.GarbageCollectorInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * An interface for a cheksum->repo path values cache
 *
 * @author Yoav Landman
 */
public interface ChecksumPaths extends ReloadableBean {

    void addChecksumPath(ChecksumPathInfo info);

    /**
     * Clears the checksum paths (marks it deleted)
     *
     * @param binaryNodeId An arbitrary string identifier used to locate the checksum path to clear
     */
    void deleteChecksumPath(String binaryNodeId);

    /**
     * Update the checksum path. This method is called when node with binary data is moved leaving the old path
     * obsolete.
     *
     * @param newChecksumInfo Checksum info of the moved binary node, with the new path.
     */
    void updateChecksumPath(ChecksumPathInfo newChecksumInfo);

    /**
     * Clean up internal deleted entries at the current point in time and return a holder of all binaries checksums that
     * need to be removed.
     *
     * @return
     */
    GarbageCollectorInfo cleanupDeleted();

    ImmutableCollection<ChecksumPathInfo> getChecksumPaths(String checksum);

    /**
     * Begin a tx if none active on the current thread
     *
     * @return true if a tx has begun by this call
     */
    boolean txBegin();

    void txEnd(boolean commit);

    /**
     * Get all files containing the like query expressions. The files that returned are guaranteed to exist at the time
     * the query was executed. For repository files prefix the path expressions with '/repositories/.
     *
     * @param fileExpressions SQL expression for the file names
     * @param pathExpressions SQL expression for the file paths
     * @return A list of paths for found files
     */
    ImmutableCollection<String> getFileOrPathsLike(@Nullable List<String> fileExpressions,
            @Nullable List<String> pathExpressions);

    /**
     * @return Gets the active checksum paths for the given checksum
     */
    ImmutableCollection<ChecksumPathInfo> getActiveChecksumPaths(@Nonnull String checksum);

    /**
     * @return Get all non-deleted (but maybe in trash) paths in the system
     */
    ImmutableCollection<ChecksumPathInfo> getAllActiveChecksumPaths();

    long getActiveSize();

    /**
     * <b>Intended for testing purposes only!</b><p/>
     * Prints the checksum paths table content. Logger must be in trace mode.
     */
    void dumpChecksumPathsTable();
}
