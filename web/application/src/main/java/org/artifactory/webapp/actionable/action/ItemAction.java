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

package org.artifactory.webapp.actionable.action;

import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.event.ItemEvent;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Yoav Landman
 */
public abstract class ItemAction extends AbstractAction {
    private String name;

    public ItemAction(String name) {
        super(name);
        this.name = name;
    }

    public abstract void onAction(ItemEvent e);

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Override and return a message to popup confirmation dialog.
     *
     * @param actionableItem ActionableItem
     * @return Confirmation message
     */
    public String getConfirmationMessage(ActionableItem actionableItem) {
        return null;
    }

    /**
     * Override and return URL to create link action. NOTICE: Application will navigate to given URL. NOTICE: Action
     * event won't be fired.
     *
     * @param actionableItem ActionableItem
     * @return action url
     */
    public String getActionLinkURL(ActionableItem actionableItem) {
        return null;
    }

    public String getDisplayName(ActionableItem actionableItem) {
        return getName();
    }

    public String getCssClass() {
        return getClass().getSimpleName();
    }

    public String getName() {
        return name;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ItemEvent event = (ItemEvent) e;
        onAction(event);

        //Fire the event to listeners
        if (isNotifyingListeners()) {
            notifyListeners(event);
        }
    }

    public boolean isNotifyingListeners() {
        return true;
    }

    public void notifyListeners(ItemEvent event) {
        ActionableItem actionableItem = event.getSource();
        actionableItem.fireActionEvent(event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemAction)) {
            return false;
        }
        ItemAction action = (ItemAction) o;
        return name.equals(action.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    public void detach() {
    }
}
