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

package org.artifactory.common.wicket.component.modal.panel.bordered;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;

/**
 * A modal panel with border and content. This panel enforces for containing a border and the border containing
 * a content.
 *
 * @author Yoav Aharoni
 */
public abstract class BorderedModalPanel extends BaseModalPanel {

    protected final Form form = new SecureForm("form");
    protected final MarkupContainer border = new TitledBorder("border");

    protected BorderedModalPanel() {
        add(form);
        form.add(border);
    }

    @Override
    public void onShow(AjaxRequestTarget target) {
        super.onShow(target);
        String markupId = getContent().getMarkupId();
        ModalHandler.bindHeightTo(markupId);
    }

    @Override
    public String getCookieName() {
        return null;
    }

    /**
     * The content included inside the border. Must have markup id.
     *
     * @return The content to include in the border
     */
    protected abstract Component getContent();

    protected abstract void addContentToBorder();
}
