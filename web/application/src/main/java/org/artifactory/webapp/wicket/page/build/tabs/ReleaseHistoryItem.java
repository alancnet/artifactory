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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.LabeledValue;
import org.artifactory.common.wicket.component.border.fieldset.FieldSetBorder;
import org.jfrog.build.api.release.PromotionStatus;

/**
 * Build release history list-view item
 *
 * @author Noam Y. Tenne
 */
public class ReleaseHistoryItem extends FieldSetBorder {

    private final PromotionStatus status;

    public ReleaseHistoryItem(String id, PromotionStatus status) {
        super(id, Model.of(status));
        this.status = status;
        add(new LabeledValue("repository", "Repository: ", status.getRepository()));
        add(new LabeledValue("comment", "Comment: ", status.getComment()));
        add(new LabeledValue("ciUser", "CI User: ", status.getCiUser()));
        add(new LabeledValue("artifactoryUser", "Artifactory User: ", status.getUser()));
    }

    @Override
    protected Component newToolbar(String id) {
        Label timestampLabel = new Label(id, "(" + ((PromotionStatus) getDefaultModelObject()).getTimestamp() + ")");
        timestampLabel.add(new CssClass("release-history-timestamp"));
        return timestampLabel;
    }

    @Override
    public String getTitle() {
        return status.getStatus();
    }
}
