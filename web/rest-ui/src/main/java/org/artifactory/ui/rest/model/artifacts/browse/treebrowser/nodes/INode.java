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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import org.artifactory.api.security.AuthorizationService;


/**
 * @author Chen Keinan
 */

public interface INode extends RestTreeNode {

    /**
     * populate action for this node by auth
     *
     * @param authService - auth service
     */
    void populateActions(AuthorizationService authService);

    /**
     * populate tabs for this node by auth
     *
     * @param authorizationService - auth service
     */
    void populateTabs(AuthorizationService authorizationService);

    /**
     * update additional child data
     */
    void updateNodeData();
}