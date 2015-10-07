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

package org.artifactory.webapp.wicket.components;

import org.apache.wicket.markup.html.list.ListItem;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.dnd.select.DragDropSelection;
import org.artifactory.webapp.wicket.util.ItemCssClass;

import java.io.Serializable;
import java.util.List;

/**
 * @author Yoav Aharoni
 */
public class IconDragDropSelection<T extends Serializable> extends DragDropSelection<T> {
    public IconDragDropSelection(String id, List<T> choices) {
        super(id, choices);
    }

    @Override
    protected void populateItem(ListItem item) {
        super.populateItem(item);

        String cssClass = getCssClass(item);
        item.add(new CssClass("icon-link " + cssClass));
    }

    protected String getCssClass(ListItem item) {
        return ItemCssClass.getRepoCssClass(item.getDefaultModelObject());
    }
}