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

package org.artifactory.common.wicket.component.modal.links;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;

/**
 * @author Yoav Aharoni
 */
public abstract class ModalShowLink extends TitledAjaxLink {
    protected ModalShowLink(String id) {
        super(id, id);
    }

    protected ModalShowLink(String id, String title) {
        super(id, title);
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
        ModalHandler modalHandler = ModalHandler.getInstanceFor(this);
        modalHandler.setModalPanel(getModelPanel());
        modalHandler.show(target);
    }

    protected abstract BaseModalPanel getModelPanel();
}
