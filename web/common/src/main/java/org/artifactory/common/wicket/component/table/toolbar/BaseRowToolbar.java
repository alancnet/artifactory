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

package org.artifactory.common.wicket.component.table.toolbar;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * @author Yoav Aharoni
 */
public class BaseRowToolbar extends AbstractToolbar {
    public BaseRowToolbar(IModel model, DataTable table) {
        super(model, table);
    }

    public BaseRowToolbar(DataTable table) {
        super(table);
    }

    {
        WebMarkupContainer tr = newRowItem("tr");
        add(tr);

        WebMarkupContainer td = newCellItem("td");
        String colSpan = String.valueOf(getTable().getColumns().size());
        td.add(new AttributeModifier("colspan", colSpan));
        tr.add(td);
    }

    public WebMarkupContainer getRowItem() {
        return (WebMarkupContainer) get("tr");
    }

    public WebMarkupContainer getCellItem() {
        return (WebMarkupContainer) getRowItem().get("td");
    }

    protected WebMarkupContainer newCellItem(String id) {
        return new WebMarkupContainer(id);
    }

    protected WebMarkupContainer newRowItem(String id) {
        return new WebMarkupContainer(id);
    }
}
