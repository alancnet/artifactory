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

package org.artifactory.common.wicket.component.modal.panel.bordered.nesting;

import org.apache.wicket.Component;
import org.artifactory.common.wicket.component.modal.panel.bordered.BorderedModalPanel;

/**
 * A modal panel that nests a panel within a border
 *
 * @author Noam Y. Tenne
 */
public class PanelNestingBorderedModal extends BorderedModalPanel {

    protected final Component content;

    public PanelNestingBorderedModal(Component content) {
        this.content = content;
        this.content.setOutputMarkupId(true);
        addContentToBorder();
    }

    @Override
    protected Component getContent() {
        return content;
    }

    @Override
    protected void addContentToBorder() {
        border.add(content);
    }
}
