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

package org.artifactory.common.wicket.component.table.groupable.header;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.sort.AjaxFallbackOrderByBorder;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.artifactory.common.wicket.ajax.CancelDefaultDecorator;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.table.groupable.column.IGroupableColumn;
import org.artifactory.common.wicket.component.table.groupable.provider.IGroupStateLocator;

import java.util.List;

/**
 * @author Yoav Aharoni
 */
public class AjaxGroupableHeadersToolbar extends AbstractToolbar {
    public AjaxGroupableHeadersToolbar(final DataTable table, final ISortStateLocator stateLocator) {
        super(table);

        // alas, copied from HeadersToolbar
        RepeatingView headers = new RepeatingView("headers");
        add(headers);

        final List<IColumn> columns = table.getColumns();
        int i = 0;
        for (final IColumn column : columns) {
            WebMarkupContainer item = new WebMarkupContainer(headers.newChildId());
            item.setRenderBodyOnly(true);
            headers.add(item);

            WebMarkupContainer header;
            if (column.isSortable()) {
                if (column instanceof IGroupableColumn) {
                    final IGroupableColumn groupableColumn = (IGroupableColumn) column;
                    header = newSortableHeader("header", groupableColumn.getSortProperty(),
                            groupableColumn.getGroupProperty(), stateLocator);
                } else {
                    header = newSortableHeader("header", column.getSortProperty(), column.getSortProperty(),
                            stateLocator);
                }
            } else {
                header = new UnsortableBorder("header");
            }

            // add IStyledColumn style
            if (column instanceof IStyledColumn) {
                header.add(new CssClass(new AbstractReadOnlyModel() {
                    @Override
                    public Object getObject() {
                        return ((IStyledColumn) column).getCssClass();
                    }
                }));
            }

            // add first/last style
            if (i == 0) {
                header.add(new CssClass("first-cell"));
            } else if (i == columns.size() - 1) {
                header.add(new CssClass("last-cell"));
            }
            item.add(header);

            // add label
            header.add(column.getHeader("label"));

            // add groupByLink
            Component groupByLink;
            WebMarkupContainer groupByText = new WebMarkupContainer("groupByText");
            if (column instanceof IGroupableColumn) {
                IGroupableColumn groupableColumn = (IGroupableColumn) column;
                String property = groupableColumn.getGroupProperty();
                groupByLink = newGroupByLink("groupByLink", stateLocator, property);

                header.add(new CssClass(new HeaderCssModel(stateLocator, property)));
            } else {
                groupByLink = new WebMarkupContainer("groupByLink").setVisible(false);
                groupByText.setVisible(false);
            }

            header.add(groupByLink);
            header.add(groupByText);
            i++;
        }
    }

    protected WebMarkupContainer newSortableHeader(String borderId, final String sortBy,
            final String groupBy,
            final ISortStateLocator stateLocator) {
        return new AjaxFallbackOrderByBorder(borderId, sortBy, stateLocator, getAjaxCallDecorator()) {
            @Override
            protected void onAjaxClick(AjaxRequestTarget target) {
                target.add(getTable());
            }

            @Override
            protected void onSortChanged() {
                super.onSortChanged();
                getTable().setCurrentPage(0);

                // reset group
                if (stateLocator instanceof IGroupStateLocator) {
                    IGroupStateLocator locator = (IGroupStateLocator) stateLocator;
                    if (isGroupedBy(locator.getGroupParam(), groupBy)) {
                        boolean ascending = locator.getGroupParam().isAscending();
                        locator.setGroupParam(new SortParam(groupBy, !ascending));
                        locator.getSortState().setPropertySortOrder("", SortOrder.NONE);
                    }
                }
            }
        };
    }

    protected IAjaxCallDecorator getAjaxCallDecorator() {
        return null;
    }


    private Component newGroupByLink(String id, final ISortStateLocator stateLocator, final String groupProperty) {
        return new AjaxLink(id) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if (stateLocator instanceof IGroupStateLocator) {
                    IGroupStateLocator groupStateLocator = (IGroupStateLocator) stateLocator;
                    switchGroupState(groupStateLocator, groupProperty);
                    target.add(getTable());
                }
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("title", "Group By");
            }

            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new CancelDefaultDecorator();
            }
        };
    }

    private static boolean isGroupedBy(SortParam groupParam, String property) {
        return property != null
                && groupParam != null
                && groupParam.getProperty() != null
                && property.equals(groupParam.getProperty());
    }

    private void switchGroupState(IGroupStateLocator groupStateLocator, String groupProperty) {
        SortParam groupParam = groupStateLocator.getGroupParam();
        if (groupParam == null || !groupProperty.equals(groupParam.getProperty())) {
            groupParam = new SortParam(groupProperty, true);
        } else {
            groupParam = null;
        }
        groupStateLocator.setGroupParam(groupParam);

        // reset sort state
        ISortState sortState = groupStateLocator.getSortState();
        if (!SortOrder.NONE.equals(sortState.getPropertySortOrder(groupProperty))) {
            if (sortState instanceof SingleSortState) {
                ((SingleSortState) sortState).setSort(null);
            }
        }
    }

    private static class HeaderCssModel extends AbstractReadOnlyModel {
        private final ISortStateLocator stateLocator;
        private final String property;

        public HeaderCssModel(ISortStateLocator stateLocator, String property) {
            this.stateLocator = stateLocator;
            this.property = property;
        }

        @Override
        public Object getObject() {
            if (stateLocator instanceof IGroupStateLocator) {
                SortParam groupParam = ((IGroupStateLocator) stateLocator).getGroupParam();
                if (!isGroupedBy(groupParam, property)) {
                    return "group-by group-by-none";
                }
                if (groupParam.isAscending()) {
                    return "group-by group-by-asc";
                }
                return "group-by group-by-desc";
            }

            return "";
        }
    }
}
