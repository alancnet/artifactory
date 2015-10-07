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

package org.artifactory.common.wicket.component.table.groupable.row;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

/**
 * @author Yoav Aharoni
 */
public class GroupRow extends Panel {

    public GroupRow(String id, Object value, final int groupSize) {
        super(id);
        add(new Label("label", Model.of(value.toString())) {
            @Override
            public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
                String body = getDefaultModelObjectAsString() + "&nbsp;<span style=\"color:grey\">(" + groupSize + ")</span>";
                replaceComponentTagBody(markupStream, openTag, body);
            }
        });
    }
}
