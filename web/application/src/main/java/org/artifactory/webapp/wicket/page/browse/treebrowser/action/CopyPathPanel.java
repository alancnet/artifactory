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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.repo.RepoPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays a list of repositories that are available to the user as a copy target
 *
 * @author Noam Y. Tenne
 */
public class CopyPathPanel extends MoveAndCopyBasePanel {
    private static final Logger log = LoggerFactory.getLogger(CopyPathPanel.class);

    @SpringBean
    private RepositoryService repoService;

    /**
     * @param id         Panel ID
     * @param pathToCopy Path to copy
     */
    public CopyPathPanel(String id, RepoPath pathToCopy) {
        super(id, pathToCopy);
        init();
    }

    @Override
    protected void refreshPage(AjaxRequestTarget target, boolean isError) {

    }

    @Override
    protected MoveMultiStatusHolder executeDryRun() {
        return moveOrCopy(true, false);
    }

    @Override
    protected MoveMultiStatusHolder moveOrCopy(boolean dryRun, boolean failFast) {
        MoveMultiStatusHolder status = new MoveMultiStatusHolder();
        try {
            return repoService.copy(sourceRepoPath, getTargetRepoPath(), dryRun, getSuppressLayout(), failFast);
        } catch(IllegalArgumentException iae) {
            status.error(String.format("Invalid path given: %s ", getTargetPath()), iae, log);
        }
        return status;
    }

    @Override
    protected OperationType getOperationType() {
        return OperationType.COPY_OPERATION;
    }
}
