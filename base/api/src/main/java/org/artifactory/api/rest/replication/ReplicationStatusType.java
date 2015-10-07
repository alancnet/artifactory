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

package org.artifactory.api.rest.replication;

import javax.annotation.Nullable;

/**
 * Replication statue available types used for repo last replication info
 *
 * @author Shay Yaakov
 */
public enum ReplicationStatusType {
    UNKNOWN("unknown", "Unknown"),
    NEVER_RUN("never_run", "Never ran"),
    INCOMPLETE("incomplete", "Incomplete"),
    ERROR("error", "Completed with errors"),
    WARN("warn", "Completed with warnings"),
    OK("ok", "Completed successfully"),
    INCONSISTENT("inconsistent", "Inconsistent");

    private String id;
    private String displayName;

    ReplicationStatusType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ReplicationStatusType findTypeById(@Nullable String id) {
        for (ReplicationStatusType replicationStatusType : values()) {
            if (replicationStatusType.getId().equals(id)) {
                return replicationStatusType;
            }
        }
        return UNKNOWN;
    }
}
