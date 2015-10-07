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

package org.artifactory.webapp.wicket.page.browse.treebrowser.action;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.wicket.page.browse.treebrowser.TreeBrowsePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This panel displays a list of local repositories the user can select to move a path to.
 *
 * @author Yossi Shaul
 */
public class MovePathPanel extends MoveAndCopyBasePanel {
    private static final Logger log = LoggerFactory.getLogger(MovePathPanel.class);

    @SpringBean
    private RepositoryService repoService;

    private final Component componentToRefresh;
    private final TreeBrowsePanel browseRepoPanel;

    public MovePathPanel(String id, RepoPath pathToMove, Component componentToRefresh,
            TreeBrowsePanel browseRepoPanel) {
        super(id, pathToMove);
        this.componentToRefresh = componentToRefresh;
        this.browseRepoPanel = browseRepoPanel;
        init();
    }

    @Override
    protected MoveMultiStatusHolder executeDryRun() {
        return moveOrCopy(true, false);
    }

    @Override
    protected MoveMultiStatusHolder moveOrCopy(boolean dryRun, boolean failFast) {
        MoveMultiStatusHolder status = new MoveMultiStatusHolder();
        try {
            status = repoService.move(sourceRepoPath, getTargetRepoPath(), dryRun, getSuppressLayout(), failFast);
        } catch (IllegalArgumentException iae) {
            status.error(String.format("Invalid path given: %s ", getTargetPath()), iae, log);
        }

        return status;
    }

    @Override
    protected OperationType getOperationType() {
        return OperationType.MOVE_OPERATION;
    }

    @Override
    protected void refreshPage(AjaxRequestTarget target, boolean isError) {

        // collapse all tree nodes
        if (componentToRefresh instanceof Tree) {
            // we collapse all since we don't know which path will eventually move
            Tree tree = (Tree) componentToRefresh;
            ITreeState treeState = tree.getTreeState();
            treeState.collapseAll();
        }

        browseRepoPanel.removeNodePanel(target);
        target.add(componentToRefresh);
        AjaxUtils.refreshFeedback(target);
    }
}