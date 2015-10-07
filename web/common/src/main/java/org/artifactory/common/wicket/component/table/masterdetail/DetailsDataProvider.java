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

package org.artifactory.common.wicket.component.table.masterdetail;

import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.List;

/**
 * @author Yoav Aharoni
 */
@SuppressWarnings({"unchecked"})
class DetailsDataProvider<M extends Serializable, D extends Serializable> extends ListDataProvider {
    private final M master;

    public DetailsDataProvider(List<D> list, M master) {
        super(list);
        this.master = master;
    }

    @Override
    public IModel model(Serializable object) {
        return new Model(new MasterDetailEntry<>(master, (D) object));
    }
}
