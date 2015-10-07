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

package org.artifactory.webapp.wicket.page.config.layout;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.LayoutsWebAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.panel.list.ModalListPanel;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.page.config.SchemaHelpModel;

import java.util.List;

/**
 * @author Yoav Aharoni
 */
public class LayoutListPanel extends ModalListPanel<RepoLayout> {

    @SpringBean
    private AddonsManager addonsManager;

    @SpringBean
    private CentralConfigService centralConfigService;

    private MutableCentralConfigDescriptor mutableDescriptor;

    public LayoutListPanel(String id) {
        super(id);
        mutableDescriptor = centralConfigService.getMutableDescriptor();
    }

    @Override
    public String getTitle() {
        return "Layouts";
    }

    @Override
    protected Component newToolbar(String id) {
        CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();
        SchemaHelpModel helpModel = new SchemaHelpModel(descriptor, "repoLayouts");
        return new SchemaHelpBubble(id, helpModel);
    }

    @Override
    protected List<RepoLayout> getList() {
        return mutableDescriptor.getRepoLayouts();
    }

    @Override
    protected void deleteItem(RepoLayout layout, AjaxRequestTarget target) {
        mutableDescriptor.removeRepoLayout(layout.getName());
        centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
    }

    @Override
    protected void addColumns(List<? super IColumn<RepoLayout>> columns) {
        columns.add(new PropertyColumn<RepoLayout>(Model.of("Name"), "name", "name"));
    }

    @Override
    public BaseModalPanel newCreateItemPanel() {
        return new LayoutCreateUpdatePanel(CreateUpdateAction.CREATE, new RepoLayout(), this);
    }

    @Override
    protected BaseModalPanel newUpdateItemPanel(RepoLayout layout) {
        return new LayoutCreateUpdatePanel(CreateUpdateAction.UPDATE, layout, this);
    }

    @Override
    protected String getDeleteConfirmationText(RepoLayout layout) {
        return "Are you sure you wish to delete the layout '" + layout.getName() + "'?";
    }

    @Override
    protected boolean canAddDeleteItemLink(RepoLayout repoLayout) {
        return !RepoLayoutUtils.isReservedName(repoLayout.getName());
    }

    @Override
    protected void addLinks(List<AbstractLink> links, RepoLayout itemObject, String linkId) {
        super.addLinks(links, itemObject, linkId);
        LayoutsWebAddon layoutsWebAddon = addonsManager.addonByType(LayoutsWebAddon.class);
        layoutsWebAddon.addLayoutCopyLink(links, itemObject, linkId, "Copy", this);
    }

    @Override
    protected AbstractLink getNewItemLink(String linkId, String linkTitle) {
        LayoutsWebAddon layoutsWebAddon = addonsManager.addonByType(LayoutsWebAddon.class);
        return layoutsWebAddon.getNewLayoutItemLink(linkId, linkTitle, this);
    }

    public MutableCentralConfigDescriptor getMutableDescriptor() {
        return mutableDescriptor;
    }
}
