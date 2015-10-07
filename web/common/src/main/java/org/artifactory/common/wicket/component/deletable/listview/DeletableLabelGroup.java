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

package org.artifactory.common.wicket.component.deletable.listview;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.component.StringChoiceRenderer;
import org.artifactory.common.wicket.component.deletable.label.DeletableLabel;
import org.artifactory.security.UserGroupInfo;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author Yoav Aharoni
 */
public class DeletableLabelGroup<T extends Serializable> extends Panel {
    private IChoiceRenderer<T> renderer;
    private boolean labelClickable = true;
    private boolean labelDeletable = true;
    private DataView dataView;

    @SuppressWarnings({"unchecked"})
    public DeletableLabelGroup(String id, Collection<T> collection) {
        this(id, new Model((Serializable) collection), null);
    }

    public DeletableLabelGroup(String id, IModel<Collection<T>> collectionModel, IChoiceRenderer<T> renderer) {
        super(id, collectionModel);
        setRenderer(renderer);
        setOutputMarkupId(true);

        dataView = new DataView<T>("item", new LabelsDataProvider()) {
            @Override
            protected void populateItem(Item<T> item) {
                final T value = item.getModelObject();
                String itemText = getDisplayValue(value);
                item.add(newLabel(value, itemText));
            }
        };
        add(dataView);
        add(new MoreIndicator("more"));
    }

    protected String getDisplayValue(T value) {
        return renderer.getDisplayValue(value).toString();
    }

    public void onDelete(T value, AjaxRequestTarget target) {
        getData().remove(value);
        target.add(this);
    }

    public IChoiceRenderer<T> getRenderer() {
        return renderer;
    }

    public void setRenderer(IChoiceRenderer<T> renderer) {
        this.renderer = renderer == null ? StringChoiceRenderer.<T>getInstance() : renderer;
    }

    public boolean isLabelClickable(T value) {
        return labelClickable;
    }

    public void setLabelClickable(boolean labelClickable) {
        this.labelClickable = labelClickable;
    }

    public boolean isLabelDeletable(T value) {
        return labelDeletable;
    }

    public void setLabelDeletable(boolean labelDeletable) {
        this.labelDeletable = labelDeletable;
    }

    public int getItemsPerPage() {
        return dataView.getItemsPerPage();
    }

    public void setItemsPerPage(int maxItems) {
        dataView.setItemsPerPage(maxItems);
    }

    private DeletableLabel newLabel(final T value, final String itemText) {
        DeletableLabel label = new DeletableLabel("label", itemText) {
            @Override
            public void onDeleteClicked(AjaxRequestTarget target) {
                onDelete(value, target);
            }
        };
        if (value instanceof UserGroupInfo) {
            UserGroupInfo userGroupInfo = (UserGroupInfo) value;
            label.setLabelClickable(!userGroupInfo.isExternal());
            label.setLabelDeletable(!userGroupInfo.isExternal());
        } else {
            label.setLabelClickable(isLabelClickable(value));
            label.setLabelDeletable(isLabelDeletable(value));
        }
        return label;
    }

    @SuppressWarnings({"unchecked"})
    public Collection<T> getData() {
        Collection<T> data = (Collection<T>) getDefaultModelObject();
        if (data == null) {
            return Collections.emptyList();
        }
        return data;
    }

    private class LabelsDataProvider implements IDataProvider<T> {
        @Override
        public Iterator<T> iterator(int first, int count) {
            // no paging anyway...
            return getData().iterator();
        }

        @Override
        public int size() {
            return getData().size();
        }

        @Override
        public IModel<T> model(T object) {
            return new Model<>(object);
        }


        @Override
        public void detach() {
        }
    }

    private class MoreIndicator extends WebMarkupContainer {
        private MoreIndicator(String id) {
            super(id);
        }

        @Override
        public boolean isVisible() {
            return super.isVisible() && dataView.getPageCount() > 1;
        }
    }
}
