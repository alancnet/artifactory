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

package org.artifactory.webapp.actionable.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.action.ItemAction;

import java.awt.event.ActionEvent;

/**
 * @author yoavl
 */
public class ItemEvent<I extends ActionableItem> extends ActionEvent {
    private ItemAction action;
    //Store a non-transient source locally
    private I source;

    public ItemEvent(I source, ItemAction action) {
        super(source, 0, action.getName());
        this.action = action;
        this.source = source;
    }

    public ItemAction getAction() {
        return action;
    }

    @Override
    public String getActionCommand() {
        return action.getName();
    }

    @Override
    public I getSource() {
        return source;
    }

    public AjaxRequestTarget getTarget() {
        return AjaxRequestTarget.get();
    }

    public ItemEventTargetComponents getTargetComponents() {
        return getSource().getEventTargetComponents();
    }
}
