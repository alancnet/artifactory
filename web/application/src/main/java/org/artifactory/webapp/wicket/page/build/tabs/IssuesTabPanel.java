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

package org.artifactory.webapp.wicket.page.build.tabs;

import com.google.common.collect.Lists;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.border.fieldset.FieldSetBorder;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.component.table.columns.BooleanColumn;
import org.artifactory.common.wicket.util.ListPropertySorter;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.Issue;
import org.jfrog.build.api.Issues;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Noam Y. Tenne
 */
public class IssuesTabPanel extends Panel {

    public IssuesTabPanel(String panelId, Build build) {
        super(panelId);

        WebMarkupContainer emptyLabel = new WebMarkupContainer("emptyLabel");
        add(emptyLabel);

        FieldSetBorder affectedIssuesBorder = new FieldSetBorder("affectedIssuesBorder");
        add(affectedIssuesBorder);

        Issues issues = build.getIssues();
        List<Issue> affectedIssues = null;
        if (issues != null) {
            Set<Issue> affectedIssuesSet = issues.getAffectedIssues();
            if (affectedIssuesSet != null) {
                affectedIssues = Lists.newArrayList(affectedIssuesSet);
            }
        }
        if (affectedIssues == null) {
            affectedIssues = Lists.newArrayList();
        }

        if (affectedIssues.isEmpty()) {
            emptyLabel.replaceWith(new Label("emptyLabel", "No issues were recorded during this build."));
            affectedIssuesBorder.setVisible(false);
        }

        List<IColumn<Issue>> columns = Lists.newArrayList();
        columns.add(new PropertyColumn<Issue>(Model.of("Key"), "key") {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                final Issue issue = (Issue) cellItem.getParent().getParent().getDefaultModelObject();
                ExternalLink link = new ExternalLink(componentId, issue.getUrl(), issue.getKey()) {
                    @Override
                    protected void onComponentTag(ComponentTag tag) {
                        super.onComponentTag(tag);
                        tag.put("onclick", "window.open('" + issue.getUrl() + "', '_blank')");
                    }
                };
                if (!issue.isAggregated()) {
                    link.add(new CssClass("bold-listed-label"));
                }
                cellItem.add(link.add(new CssClass("item-link")));
            }
        });
        columns.add(new PropertyColumn<Issue>(Model.of("Summary"), "summary"));
        columns.add(new BooleanColumn<Issue>(Model.of("Previous Build"), "aggregated"));

        IssueDataProvider dataProvider = new IssueDataProvider(affectedIssues);

        affectedIssuesBorder.add(new SortableTable<>("issues", columns, dataProvider, 50));
    }

    private static class IssueDataProvider extends SortableDataProvider<Issue> {

        List<Issue> issueList;

        /**
         * Main constructor
         *
         * @param affectedIssues Modules to display
         */
        public IssueDataProvider(List<Issue> affectedIssues) {
            setSort("aggregated", SortOrder.ASCENDING);
            issueList = (affectedIssues != null) ? affectedIssues : Lists.<Issue>newArrayList();
        }

        @Override
        public Iterator<Issue> iterator(int first, int count) {
            ListPropertySorter.sort(issueList, getSort());
            List<Issue> listToReturn = issueList.subList(first, first + count);
            return listToReturn.iterator();
        }

        @Override
        public int size() {
            return issueList.size();
        }

        @Override
        public IModel<Issue> model(Issue object) {
            return new Model<>(object);
        }
    }
}
