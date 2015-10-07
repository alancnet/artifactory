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

package org.artifactory.webapp.wicket.actionable.tree;

import org.apache.wicket.markup.html.panel.Panel;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.model.HierarchicActionableItem;

import java.util.List;

/**
 * @author Yoav Landman
 */
public interface ActionableItemsProvider {
    List<? extends ActionableItem> getChildren(HierarchicActionableItem parent);

    boolean hasChildren(HierarchicActionableItem parent);

    HierarchicActionableItem getRoot();

    Panel getItemDisplayPanel();

    void setItemDisplayPanel(Panel panel);
}
