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

package org.artifactory.common.wicket.component.modal.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;

import java.io.Serializable;

/**
 * Interface for actions to perform when refreshing after editing property values
 *
 * @author Noam Y. Tenne
 */
public interface EditValueButtonRefreshBehavior extends Serializable {

    /**
     * Refreshes display
     *
     * @param valueStrings      Property value strings
     * @param ajaxRequestTarget Ajax request target
     */
    void refresh(String[] valueStrings, AjaxRequestTarget ajaxRequestTarget);
}
