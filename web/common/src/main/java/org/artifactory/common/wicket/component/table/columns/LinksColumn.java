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

package org.artifactory.common.wicket.component.table.columns;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.component.table.columns.panel.links.LinksColumnPanel;

import java.util.Collection;

import static org.artifactory.common.wicket.component.panel.links.LinksPanel.LINK_ID;

/**
 * @author Yoav Aharoni
 */
public abstract class LinksColumn<T> extends AbstractColumn<T> implements AttachColumnListener {
    protected LinksColumn() {
        this(Model.of(""));
    }

    protected LinksColumn(String title) {
        this(Model.of(title));
    }

    protected LinksColumn(IModel<String> titleModel) {
        super(titleModel);
    }

    @Override
    public void populateItem(Item cellItem, String componentId, IModel rowModel) {
        cellItem.add(new CssClass("actions"));

        LinksColumnPanel panel = new LinksColumnPanel(componentId);
        cellItem.add(panel);

        Collection<? extends AbstractLink> links = getLinks(getRowObject(rowModel), LINK_ID);
        for (AbstractLink link : links) {
            panel.addLink(link);
        }
    }

    @SuppressWarnings({"unchecked"})
    protected T getRowObject(IModel rowModel) {
        return (T) rowModel.getObject();
    }

    @SuppressWarnings({"RefusedBequest"})
    @Override
    public String getCssClass() {
        return LinksColumn.class.getSimpleName();
    }

    protected abstract Collection<? extends AbstractLink> getLinks(T rowObject, String linkId);

    @Override
    public void onColumnAttached(SortableTable table) {
        table.add(new CssClass("selectable"));
    }
}
