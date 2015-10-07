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

package org.artifactory.webapp.actionable.action;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.event.RepoAwareItemEvent;

/**
 * @author yoavl
 */
public class ZapAction extends RepoAwareItemAction {
    public static final String ACTION_NAME = "Zap Caches";

    public ZapAction() {
        super(ACTION_NAME);
    }

    @Override
    public void onAction(RepoAwareItemEvent e) {
        RepoPath repoPath = e.getRepoPath();
        getRepoService().zap(repoPath);
        AjaxRequestTarget target = e.getTarget();
        WebMarkupContainer nodePanelContainer = e.getTargetComponents().getNodePanelContainer();
        target.add(nodePanelContainer);
        Page page = target.getPage();
        page.info("Completed zapping item: '" + repoPath + "'");
    }
}