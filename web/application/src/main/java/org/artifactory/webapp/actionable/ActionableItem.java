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

package org.artifactory.webapp.actionable;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.actionable.action.ItemActionListener;
import org.artifactory.webapp.actionable.event.ItemEvent;
import org.artifactory.webapp.actionable.event.ItemEventTargetComponents;

import java.util.List;
import java.util.Set;

/**
 * @author Yoav Landman
 */
public interface ActionableItem extends IModel {
    /**
     * @param id The panel id
     * @return A new panel to be displayed in the right side of the actionable item
     */
    Panel newItemDetailsPanel(String id);

    Set<ItemAction> getActions();

    Set<ItemAction> getContextMenuActions();

    String getDisplayName();

    String getCssClass();

    void filterActions(AuthorizationService authService);

    List<ItemActionListener> getListeners();

    void addActionListener(ItemActionListener listener);

    void removeActionListener(ItemActionListener listener);

    void fireActionEvent(ItemEvent e);

    ItemEventTargetComponents getEventTargetComponents();

    void setEventTargetComponents(ItemEventTargetComponents targetComponents);
}
