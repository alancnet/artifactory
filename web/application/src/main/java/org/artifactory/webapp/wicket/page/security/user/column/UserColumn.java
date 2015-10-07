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

package org.artifactory.webapp.wicket.page.security.user.column;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.component.table.columns.TitlePropertyColumn;
import org.artifactory.webapp.wicket.page.security.user.UserModel;

/**
 * @author Yoav Aharoni
 */
public class UserColumn extends TitlePropertyColumn<UserModel> {
    public UserColumn(String title) {
        super(title, "username", "username");
    }

    @Override
    public void populateItem(Item<ICellPopulator<UserModel>> item, String componentId, IModel<UserModel> model) {
        item.add(new UsernamePanel(componentId, model));
    }
}
