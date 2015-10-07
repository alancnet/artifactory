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

package org.artifactory.webapp.wicket.actionable.column;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.table.columns.LinksColumn;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.wicket.actionable.link.ActionLink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Yoav Aharoni
 */
public class ActionsColumn<T extends ActionableItem> extends LinksColumn<T> {
    @SpringBean
    private AuthorizationService authorizationService;

    public ActionsColumn(String title) {
        this(Model.of(title));
    }

    public ActionsColumn(IModel<String> titleModel) {
        super(titleModel);
        Injector.get().inject(this);
    }

    protected Collection<ItemAction> getActions(T actionableItem) {
        Set<ItemAction> actions = actionableItem.getActions();
        actionableItem.filterActions(authorizationService);
        return actions;
    }


    @Override
    protected Collection<? extends AbstractLink> getLinks(final T actionableItem, String linkId) {
        List<TitledAjaxLink> links = new ArrayList<>();
        for (final ItemAction action : getActions(actionableItem)) {
            if (action.isEnabled()) {
                links.add(new ActionLink(linkId, action, actionableItem));
            }
        }
        return links;
    }
}
