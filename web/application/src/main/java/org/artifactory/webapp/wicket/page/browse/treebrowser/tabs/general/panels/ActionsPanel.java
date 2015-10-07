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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.general.panels;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.artifactory.common.wicket.component.border.fieldset.FieldSetBorder;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.wicket.actionable.link.ActionLink;

import java.util.Set;

/**
 * This panel displays action buttons available for an item (file or folder) in the tree browser.
 *
 * @author Yossi Shaul
 */
public class ActionsPanel extends Panel {
    private static final int LINK_ROWS = 2;

    public ActionsPanel(String id, ActionableItem repoItem) {
        super(id);
        addActions(repoItem);
    }

    private void addActions(final ActionableItem repoItem) {
        Set<ItemAction> actions = repoItem.getActions();
        final int rowCount = Math.min(LINK_ROWS, actions.size());

        // add border
        FieldSetBorder actionBorder = new FieldSetBorder("actionBorder");
        add(actionBorder);

        // add rowsView
        final RepeatingView rowsView = new RepeatingView("rows");
        actionBorder.add(rowsView);

        // add rows
        RepeatingView[] rowsArray = new RepeatingView[rowCount];
        for (int i = 0; i < rowCount; i++) {
            final WebMarkupContainer rowContainer = new WebMarkupContainer(String.valueOf(i));
            rowsView.add(rowContainer);

            final RepeatingView rowView = new RepeatingView("action");
            rowContainer.add(rowView);
            rowsArray[i] = rowView;
        }

        // filter enabled actions and add them to rows
        int i = 0;
        for (ItemAction action : actions) {
            if (action.isEnabled()) {
                final RepeatingView rowView = rowsArray[i % rowCount];
                final WebMarkupContainer li = new WebMarkupContainer(String.valueOf(i));
                rowView.add(li);

                li.add(new ActionLink("link", action, repoItem));
                i++;
            }
        }

        actionBorder.setVisible(i > 0);
    }

}
