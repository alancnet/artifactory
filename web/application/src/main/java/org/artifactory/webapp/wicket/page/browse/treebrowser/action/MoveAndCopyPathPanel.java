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

package org.artifactory.webapp.wicket.page.browse.treebrowser.action;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.help.HelpBubble;

import static org.artifactory.common.wicket.util.ComponentPersister.setPersistent;

/**
 * Input for the path that artifacts should be copied/moved to
 *
 * @author Dan Feldman
 */
public class MoveAndCopyPathPanel extends Panel {

    protected static final String TITLE_KEY = "panel.title";

    protected static final String COPY_OP_KEY = "targetPathCopy.help";

    protected static final String MOVE_OP_KEY = "targetPathMove.help";

    private MoveAndCopyBasePanel.OperationType opType;

    public MoveAndCopyPathPanel(String id, IModel targetRepoModel, final MoveAndCopyBasePanel.OperationType opType) {
        super(id);
        this.opType = opType;
        init();
        add(new CssClass("advanced-search-panel"));

        TextField targetPathField = new TextField<>("targetPath", targetRepoModel);
        targetPathField.setOutputMarkupId(true);
        setPersistent(targetPathField);
        targetPathField.setEnabled(true);
        add(targetPathField);

        setVisible(false);
    }

    private void init() {
        setOutputMarkupId(true);
        add(new Label("title", getString(TITLE_KEY, null)));
        add(newHelpBubble("operation.help"));
    }

    protected Component newHelpBubble(String id) {
            if (opType.equals(MoveAndCopyBasePanel.OperationType.COPY_OPERATION)) {
                return new HelpBubble(id, new ResourceModel(COPY_OP_KEY));
            } else if (opType.equals(MoveAndCopyBasePanel.OperationType.MOVE_OPERATION)) {
                return new HelpBubble(id, new ResourceModel(MOVE_OP_KEY));
            }
        return new HelpBubble(id, Model.of(""));    //fallback
    }
}
