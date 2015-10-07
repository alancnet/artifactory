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

package org.artifactory.webapp.wicket.panel.tabbed;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.artifactory.common.wicket.util.AjaxUtils;

import java.util.List;

/**
 * @author Yoav Aharoni
 */
public class SubmittingTabbedPanel extends StyledTabbedPanel {
    public SubmittingTabbedPanel(String id, List<ITab> tabs) {
        super(id, tabs);
    }

    @Override
    protected WebMarkupContainer createLink(String linkId, final int index) {
        return new AjaxSubmitLink(linkId) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                setSelectedTab(index);
                target.add(SubmittingTabbedPanel.this);
                onAjaxUpdate(target);
                AjaxUtils.refreshFeedback(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form form) {
                AjaxUtils.refreshFeedback(target);
            }
        };
    }
}
