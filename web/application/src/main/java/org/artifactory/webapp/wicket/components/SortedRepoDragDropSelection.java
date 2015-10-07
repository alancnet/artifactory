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

package org.artifactory.webapp.wicket.components;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;

import java.io.Serializable;
import java.util.List;

/**
 * An icon supporting sorted drag drop selection that implements an item sorting order based on the type of repository
 * descriptor which is the list item.
 *
 * @author Noam Tenne
 */
public class SortedRepoDragDropSelection<T extends Serializable> extends IconSortedDragDropSelection<T> {

    public SortedRepoDragDropSelection(String id, List<T> choices) {
        super(id, choices);
    }

    public SortedRepoDragDropSelection(String id, IModel<T> model, List<T> choices) {
        super(id, model, choices);
    }

    @Override
    protected String getSortValue(ListItem item) {
        Object modelObject = item.getDefaultModelObject();

        /**
         * Implement a special sorting order if the list items are repo descriptors
         */
        if (modelObject instanceof LocalCacheRepoDescriptor) {
            return "3";
        } else if (modelObject instanceof LocalRepoDescriptor) {
            return "1";
        } else if (modelObject instanceof HttpRepoDescriptor) {
            return "2";
        } else if (modelObject instanceof VirtualRepoDescriptor) {
            return "4";
        }
        return super.getSortValue(item);
    }
}
