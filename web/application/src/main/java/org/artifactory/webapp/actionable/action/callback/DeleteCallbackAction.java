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

package org.artifactory.webapp.actionable.action.callback;

import org.artifactory.webapp.actionable.action.DeleteAction;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.actionable.event.ItemEvent;

/**
 * Item action that uses a callback for an on-delete action
 *
 * @author Noam Tenne
 */
public class DeleteCallbackAction extends ItemAction {

    public static final String ACTION_NAME = "Delete";
    private DeleteCallback callback;

    public DeleteCallbackAction(DeleteCallback callback) {
        super(ACTION_NAME);
        this.callback = callback;
    }

    @Override
    public void onAction(ItemEvent e) {
        if (callback != null) {
            callback.onDelete(e);
        }
    }

    @Override
    public String getCssClass() {
        return DeleteAction.class.getSimpleName();
    }
}