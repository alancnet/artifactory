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

package org.artifactory.webapp.wicket.page.build.tabs.list;

import com.google.common.collect.Lists;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.webapp.wicket.page.build.actionable.ModuleArtifactActionableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The disabled module artifacts list panel
 *
 * @author Noam Y. Tenne
 */
public class DisabledModuleArtifactsListPanel extends BaseModuleArtifactsListPanel {

    private static final Logger log = LoggerFactory.getLogger(DisabledModuleArtifactsListPanel.class);

    /**
     * Main constructor
     *
     * @param id ID to assign to the panel
     */
    public DisabledModuleArtifactsListPanel(String id) {
        super(id);

        try {
            addTable();
        } catch (RepositoryRuntimeException rre) {
            String errorMessage = "An error occurred while loading the produced artifact list";
            log.error(errorMessage, rre);
            error(errorMessage);
        }
    }

    @Override
    public List<ModuleArtifactActionableItem> getArtifacts() {
        return Lists.newArrayList();
    }
}