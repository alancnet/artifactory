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

package org.artifactory.build.staging;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * @author Noam Y. Tenne
 */
public class VcsConfig implements Serializable {

    private boolean useReleaseBranch;
    private String releaseBranchName;
    private boolean createTag;
    private String tagUrlOrName;
    private String tagComment;
    private String nextDevelopmentVersionComment;

    public boolean isUseReleaseBranch() {
        return useReleaseBranch;
    }

    public void setUseReleaseBranch(boolean useReleaseBranch) {
        this.useReleaseBranch = useReleaseBranch;
    }

    @Nullable
    public String getReleaseBranchName() {
        return releaseBranchName;
    }

    public void setReleaseBranchName(@Nullable String releaseBranchName) {
        this.releaseBranchName = releaseBranchName;
    }

    public boolean isCreateTag() {
        return createTag;
    }

    public void setCreateTag(boolean createTag) {
        this.createTag = createTag;
    }

    @Nullable
    public String getTagUrlOrName() {
        return tagUrlOrName;
    }

    public void setTagUrlOrName(@Nullable String tagUrlOrName) {
        this.tagUrlOrName = tagUrlOrName;
    }

    @Nullable
    public String getTagComment() {
        return tagComment;
    }

    public void setTagComment(@Nullable String tagComment) {
        this.tagComment = tagComment;
    }

    @Nullable
    public String getNextDevelopmentVersionComment() {
        return nextDevelopmentVersionComment;
    }

    public void setNextDevelopmentVersionComment(@Nullable String nextDevelopmentVersionComment) {
        this.nextDevelopmentVersionComment = nextDevelopmentVersionComment;
    }
}
