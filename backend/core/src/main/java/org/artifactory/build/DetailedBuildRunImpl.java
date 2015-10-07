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

import com.google.common.collect.Lists;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.release.PromotionStatus;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * A detailed build run info holder
 *
 * @author Noam Y. Tenne
 */
public class DetailedBuildRunImpl implements DetailedBuildRun {

    final Build build;

    public DetailedBuildRunImpl(@Nonnull Build build) {
        this.build = build;
    }

    @Override
    public String getName() {
        return build.getName();
    }

    @Override
    public String getNumber() {
        return build.getNumber();
    }

    @Override
    public String getStarted() {
        return build.getStarted();
    }

    @Override
    public Date getStartedDate() {
        return new Date(BuildInfoUtils.parseBuildTime(getStarted()));
    }

    @Override
    public String getBuildAgent() {
        return build.getBuildAgent().toString();
    }

    @Override
    public String getAgent() {
        return build.getAgent().toString();
    }

    @Override
    public long getDurationMillis() {
        return build.getDurationMillis();
    }

    @Override
    public String getPrincipal() {
        return build.getPrincipal();
    }

    @Override
    public String getArtifactoryPrincipal() {
        return build.getArtifactoryPrincipal();
    }

    @Override
    public String getUrl() {
        return build.getUrl();
    }

    @Override
    public String getParentName() {
        return build.getParentName();
    }

    @Override
    public String getParentNumber() {
        return build.getParentNumber();
    }

    @Override
    public String getVcsRevision() {
        return build.getVcsRevision();
    }

    @Override
    @Nonnull
    public List<Module> getModules() {
        List<Module> modulesToReturn = Lists.newArrayList();
        List<org.jfrog.build.api.Module> buildModules = build.getModules();
        if (buildModules != null) {
            for (org.jfrog.build.api.Module buildModule : buildModules) {
                modulesToReturn.add(new Module(buildModule));
            }
        }
        return modulesToReturn;
    }

    @Override
    @Nonnull
    public DetailedBuildRun copy() {
        return copy(null);
    }

    @Override
    @Nonnull
    public DetailedBuildRun copy(String buildNumber) {
        Build copy = (Build) SerializationUtils.clone(build);
        copy.setStartedDate(new Date());
        if (StringUtils.isNotBlank(buildNumber)) {
            copy.setNumber(buildNumber);
        }
        return new DetailedBuildRunImpl(copy);
    }

    @Override
    public List<ReleaseStatus> getReleaseStatuses() {
        List<PromotionStatus> promotionStatuses = build.getStatuses();
        if (promotionStatuses == null) {
            promotionStatuses = Lists.newArrayList();
            build.setStatuses(promotionStatuses);
        }
        return new ReleaseStatusList(promotionStatuses);
    }

    @Override
    public String getCiUrl() {
        return build.getUrl();
    }

    @Override
    public String getReleaseStatus() {
        List<PromotionStatus> statuses = build.getStatuses();
        if (statuses == null) {
            return null;
        }
        return Collections.max(statuses, new Comparator<PromotionStatus>() {
            @Override
            public int compare(PromotionStatus o1, PromotionStatus o2) {
                return o1.getTimestampDate().compareTo(o2.getTimestampDate());
            }
        }).getStatus();
    }
}