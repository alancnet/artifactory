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

package org.artifactory.build;

import org.jfrog.build.api.builder.PromotionStatusBuilder;
import org.jfrog.build.api.release.PromotionStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Noam Y. Tenne
 */
public class ReleaseStatus implements Serializable {

    private PromotionStatus promotionStatus;

    public ReleaseStatus(@Nonnull String status, @Nullable String comment, @Nullable String repository,
            @Nullable String ciUser, @Nullable String artifactoryUser) {
        this(new PromotionStatusBuilder(status).timestampDate(new Date()).comment(comment).repository(repository)
                .ciUser(ciUser).user(artifactoryUser).build());
    }

    ReleaseStatus(PromotionStatus promotionStatus) {
        this.promotionStatus = promotionStatus;
    }

    @Nonnull
    public String getStatus() {
        return promotionStatus.getStatus();
    }

    @Nonnull
    public String getTimestamp() {
        return promotionStatus.getTimestamp();
    }

    @Nonnull
    public Date getTimestampDate() {
        return promotionStatus.getTimestampDate();
    }

    @Nullable
    public String getComment() {
        return promotionStatus.getComment();
    }

    @Nullable
    public String getRepository() {
        return promotionStatus.getRepository();
    }

    @Nullable
    public String getCiUser() {
        return promotionStatus.getCiUser();
    }

    @Nullable
    public String getArtifactoryUser() {
        return promotionStatus.getUser();
    }

    @Nonnull
    PromotionStatus getPromotionStatus() {
        return promotionStatus;
    }
}
