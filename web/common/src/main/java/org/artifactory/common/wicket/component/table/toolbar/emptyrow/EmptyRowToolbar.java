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

package org.artifactory.common.wicket.component.table.toolbar.emptyrow;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.table.toolbar.BaseRowToolbar;

/**
 * @author Eli Givoni
 */
public class EmptyRowToolbar extends BaseRowToolbar {
    public EmptyRowToolbar(DataTable table) {
        super(table);
        getRowItem().add(new CssClass("space-row"));
        getCellItem().add(new CssClass("first-cell last-cell"));
    }
}
