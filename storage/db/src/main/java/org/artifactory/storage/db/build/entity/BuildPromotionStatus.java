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

package org.artifactory.storage.db.build.entity;

import org.apache.commons.lang.StringUtils;

/**
 * Date: 11/23/12
 * Time: 8:50 AM
 *
 * @author freds
 */
public class BuildPromotionStatus implements Comparable<BuildPromotionStatus> {
    private final long buildId;
    private final long created;
    private final String createdBy;
    private final String status;
    private final String repository;
    private final String comment;
    private final String ciUser;

    public BuildPromotionStatus(long buildId, long created, String createdBy, String status, String repository,
            String comment, String ciUser) {
        if (buildId <= 0L) {
            throw new IllegalArgumentException("Build id cannot be zero or negative!");
        }
        if (StringUtils.isBlank(status)) {
            throw new IllegalArgumentException("Promotion status cannot be empty or null!");
        }
        if (created <= 0L) {
            throw new IllegalArgumentException("Created date cannot be zero or negative!");
        }
        this.buildId = buildId;
        this.created = created;
        this.createdBy = createdBy;
        this.status = status;
        this.repository = repository;
        this.comment = comment;
        this.ciUser = ciUser;
    }

    public long getBuildId() {
        return buildId;
    }

    public long getCreated() {
        return created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getStatus() {
        return status;
    }

    public String getRepository() {
        return repository;
    }

    public String getComment() {
        return comment;
    }

    public String getCiUser() {
        return ciUser;
    }

    public boolean isIdentical(BuildPromotionStatus o) {
        if (this == o) {
            return true;
        }
        if (o == null || !this.equals(o)) {
            return false;
        }
        return StringUtils.equals(createdBy, o.createdBy)
                && StringUtils.equals(status, o.status)
                && StringUtils.equals(repository, o.repository)
                && StringUtils.equals(comment, o.comment)
                && StringUtils.equals(ciUser, o.ciUser);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BuildPromotionStatus that = (BuildPromotionStatus) o;

        if (buildId != that.buildId) {
            return false;
        }
        if (created != that.created) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (buildId ^ (buildId >>> 32));
        result = 31 * result + (int) (created ^ (created >>> 32));
        return result;
    }

    @Override
    public int compareTo(BuildPromotionStatus o) {
        if (buildId != o.buildId) {
            return Long.compare(buildId, o.buildId);
        }
        return Long.compare(created, o.created);
    }
}
