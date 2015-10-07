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

package org.artifactory.common.wicket.component.panel.feedback.aggregated;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.JavascriptEvent;
import org.artifactory.common.wicket.component.panel.feedback.FeedbackMessagesPanel;
import org.artifactory.common.wicket.contributor.ResourcePackage;

/**
 * @author Yoav Aharoni
 */
public class AggregateFeedbackPanel extends FeedbackMessagesPanel {
    public AggregateFeedbackPanel(String id) {
        super(id);

        add(new CssClass("aggregate-feedback"));
        add(newResourcePackage());
        add(new JavascriptEvent("onclear", new ScriptModel("onClear")));
        add(new JavascriptEvent("onshow", new ScriptModel("onShow")));
    }

    protected ResourcePackage newResourcePackage() {
        return ResourcePackage.forJavaScript(AggregateFeedbackPanel.class);
    }

    private class ScriptModel extends AbstractReadOnlyModel {
        private String function;

        private ScriptModel(String function) {
            this.function = function;
        }

        @Override
        public Object getObject() {
            return "AggregateFeedbackPanel." + function + "('" + getMarkupId() + "')";
        }
    }
}
