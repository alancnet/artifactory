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

package org.artifactory.webapp.actionable.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.actionable.action.RepoAwareItemAction;

/**
 * @author yoavl
 */
public class RepoAwareItemEvent extends ItemEvent<RepoAwareActionableItem> {

    public RepoAwareItemEvent(RepoAwareActionableItem source, RepoAwareItemAction action,
            AjaxRequestTarget target) {
        super(source, action);
    }

    public RepoPath getRepoPath() {
        return getSource().getRepoPath();
    }
}