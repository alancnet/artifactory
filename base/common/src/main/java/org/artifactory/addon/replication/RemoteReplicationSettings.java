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

package org.artifactory.addon.replication;

import org.artifactory.repo.RepoPath;

import java.io.Writer;

/**
 * @author Noam Y. Tenne
 */
public class RemoteReplicationSettings extends ReplicationBaseSettings {

    private final boolean progress;
    private final ReplicationAddon.Overwrite overwrite;

    /**
     * Never used ?!?!
     */
    @Deprecated
    private final int mark;

    /**
     * Never used ?!?!
     */
    @Deprecated
    private final Writer responseWriter;

    /**
     * <B>NOTE<B>: Try to refrain from using this constructor directly and use the builder instead
     */
    protected RemoteReplicationSettings(RepoPath repoPath, boolean progress, int mark, boolean deleteExisting,
            boolean includeProperties, ReplicationAddon.Overwrite overwrite, Writer responseWriter,
            String url, int socketTimeoutMillis) {
        super(repoPath, deleteExisting, includeProperties, url, socketTimeoutMillis);
        this.progress = progress;
        this.mark = mark;
        this.overwrite = overwrite;
        this.responseWriter = responseWriter;
    }

    public boolean isProgress() {
        return progress;
    }

    /**
     * Never used ?!?!
     */
    @Deprecated
    public int getMark() {
        return mark;
    }

    public ReplicationAddon.Overwrite getOverwrite() {
        return overwrite;
    }

    /**
     * Never used ?!?!
     */
    @Deprecated
    public Writer getResponseWriter() {
        return responseWriter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RemoteReplicationSettings)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        RemoteReplicationSettings that = (RemoteReplicationSettings) o;

        if (mark != that.mark) {
            return false;
        }
        if (progress != that.progress) {
            return false;
        }
        if (overwrite != that.overwrite) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (progress ? 1 : 0);
        result = 31 * result + mark;
        result = 31 * result + (overwrite != null ? overwrite.hashCode() : 0);
        return result;
    }
}