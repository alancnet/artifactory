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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.release.PromotionStatus;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Displays the selected build's release history
 *
 * @author Noam Y. Tenne
 */
public class ReleaseHistoryTabPanel extends Panel {

    public ReleaseHistoryTabPanel(String id, Build build) {
        super(id);

        WebMarkupContainer emptyLabel = new WebMarkupContainer("emptyLabel");
        add(emptyLabel);

        List<PromotionStatus> statuses = build.getStatuses();
        if (statuses == null) {
            statuses = Lists.newArrayList();
        }

        if (statuses.isEmpty()) {
            emptyLabel.replaceWith(new Label("emptyLabel", "This build has no release history."));
        } else {
            Comparator<PromotionStatus> reverseDate = Collections.reverseOrder(new Comparator<PromotionStatus>() {

                @Override
                public int compare(PromotionStatus o1, PromotionStatus o2) {
                    return o1.getTimestampDate().compareTo(o2.getTimestampDate());
                }
            });
            Collections.sort(statuses, reverseDate);
        }

        add(new ListView<PromotionStatus>("historyList", statuses) {

            @Override
            protected void populateItem(ListItem<PromotionStatus> statusListItem) {
                statusListItem.add(new ReleaseHistoryItem("historyItem", statusListItem.getModelObject()));
            }
        });
    }
}
