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

package org.artifactory.webapp.wicket.actionable.link;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.ajax.AllowNewWindowDecorator;
import org.artifactory.common.wicket.ajax.ConfirmationAjaxCallDecorator;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.actionable.action.RepoAwareItemAction;
import org.artifactory.webapp.actionable.event.ItemEvent;
import org.artifactory.webapp.actionable.event.RepoAwareItemEvent;

/**
 * @author Yoav Aharoni
 */
public class ActionLink extends TitledAjaxLink {
    private ActionableItem actionableItem;
    private ItemAction action;

    public ActionLink(String id, ItemAction action, ActionableItem actionableItem) {
        super(id, Model.of(action.getDisplayName(actionableItem)));
        this.action = action;
        this.actionableItem = actionableItem;

        // add css classes
        add(new CssClass("icon-link"));
        add(new CssClass(action.getCssClass()));

        // set href for link actions
        String actionURL = action.getActionLinkURL(actionableItem);
        if (StringUtils.isNotEmpty(actionURL)) {
            add(new AttributeModifier("onclick", "window.open(this.href); return false;"));
            add(new AttributeModifier("href", actionURL));
        }
    }

    @Override
    public boolean isEnabled() {
        return action.isEnabled();
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
        action.actionPerformed(newEvent(target));
    }

    @Override
    protected IAjaxCallDecorator getAjaxCallDecorator() {
        String message = action.getConfirmationMessage(actionableItem);
        return new AllowNewWindowDecorator(new ConfirmationAjaxCallDecorator(message));
    }

    protected ItemEvent newEvent(AjaxRequestTarget target) {
        if (actionableItem instanceof RepoAwareActionableItem && action instanceof RepoAwareItemAction) {
            RepoAwareActionableItem repoAwareItem = (RepoAwareActionableItem) actionableItem;
            RepoAwareItemAction repoAwareAction = (RepoAwareItemAction) action;
            return new RepoAwareItemEvent(repoAwareItem, repoAwareAction, target);
        }

        return new ItemEvent(actionableItem, action);
    }
}