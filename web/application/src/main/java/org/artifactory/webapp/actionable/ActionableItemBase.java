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

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.actionable.action.ItemActionListener;
import org.artifactory.webapp.actionable.event.ItemEvent;
import org.artifactory.webapp.actionable.event.ItemEventTargetComponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Yoav Landman
 */
public abstract class ActionableItemBase extends AbstractReadOnlyModel implements ActionableItem {

    private Set<ItemAction> actions = new LinkedHashSet<>();
    private final List<ItemActionListener> listeners;
    private ItemEventTargetComponents eventTargetComponents;

    public ActionableItemBase() {
        this.listeners = Collections.synchronizedList(new ArrayList<ItemActionListener>());
    }

    @Override
    public Set<ItemAction> getActions() {
        return actions;
    }

    @Override
    public Set<ItemAction> getContextMenuActions() {
        return actions;
    }

    @Override
    public List<ItemActionListener> getListeners() {
        return listeners;
    }

    @Override
    public void addActionListener(ItemActionListener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
    }

    @Override
    public void detach() {
        super.detach();

        for (ItemAction action : getActions()) {
            action.detach();
        }
    }

    @Override
    public void removeActionListener(ItemActionListener listener) {
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
    }

    @Override
    public void fireActionEvent(ItemEvent e) {
        //Fire the event to listeners
        synchronized (listeners) {
            for (ItemActionListener listener : listeners) {
                listener.actionPerformed(e);
            }
        }
    }

    @Override
    public ItemEventTargetComponents getEventTargetComponents() {
        return eventTargetComponents;
    }

    @Override
    public void setEventTargetComponents(ItemEventTargetComponents eventTargetComponents) {
        this.eventTargetComponents = eventTargetComponents;
    }

    @Override
    public ActionableItem getObject() {
        return this;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
