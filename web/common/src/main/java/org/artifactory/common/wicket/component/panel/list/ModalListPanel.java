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

package org.artifactory.common.wicket.component.panel.list;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.modal.links.ModalShowLink;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;

import java.io.Serializable;

/**
 * A BaseListPanel implementation which opens the create and update panels in a modal pop-up window
 *
 * @author Yoav Aharoni
 */
public abstract class ModalListPanel<T extends Serializable> extends BaseListPanel<T> {

    protected ModalListPanel(String id) {
        super(id);
        init();
    }

    protected ModalListPanel(String id, SortableDataProvider<T> dataProvider) {
        super(id, dataProvider);
        init();
    }

    @Override
    protected AbstractLink getNewItemLink(String linkId, String linkTitle) {
        return new ModalShowLink(linkId, linkTitle) {
            @Override
            protected BaseModalPanel getModelPanel() {
                return newCreateItemPanel();
            }
        };
    }

    @Override
    protected TitledAjaxLink getEditItemLink(final T itemObject, String linkId) {
        return new ModalShowLink(linkId, "Edit") {
            @Override
            protected BaseModalPanel getModelPanel() {
                return newUpdateItemPanel(itemObject);
            }
        };
    }

    @Override
    protected void onRowItemEvent(String id, int index, final IModel model, AjaxRequestTarget target) {
        ModalHandler modalHandler = ModalHandler.getInstanceFor(ModalListPanel.this);
        @SuppressWarnings({"unchecked"})
        T itemObject = (T) model.getObject();
        modalHandler.setModalPanel(newUpdateItemPanel(itemObject));
        modalHandler.show(target);
    }

    protected abstract BaseModalPanel newCreateItemPanel();

    protected abstract BaseModalPanel newUpdateItemPanel(T itemObject);
}
