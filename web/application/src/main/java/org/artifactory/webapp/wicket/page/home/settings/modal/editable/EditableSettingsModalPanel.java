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

package org.artifactory.webapp.wicket.page.home.settings.modal.editable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.StringResourceStream;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.FilteredResourcesWebAddon;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonStyleModel;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.modal.links.ModalCloseLink;
import org.artifactory.common.wicket.component.modal.panel.bordered.BorderedModalPanel;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.webapp.wicket.page.home.settings.modal.DownloadModalSettings;
import org.artifactory.webapp.wicket.page.home.settings.modal.download.AjaxSettingsDownloadBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

/**
 * Editable build-tool settings modal
 *
 * @author Noam Y. Tenne
 */
public class EditableSettingsModalPanel extends BorderedModalPanel {

    private static final Logger log = LoggerFactory.getLogger(EditableSettingsModalPanel.class);

    @SpringBean
    private AddonsManager addonsManager;
    private final TextArea<String> contentTextArea;

    public EditableSettingsModalPanel(final DownloadModalSettings settings) {
        contentTextArea = new TextArea<>("content", Model.of(settings.getContent()));
        contentTextArea.setOutputMarkupId(true);

        FilteredResourcesWebAddon filteredResourcesWebAddon =
                addonsManager.addonByType(FilteredResourcesWebAddon.class);
        String saveToFileName = settings.getSaveToFileName();
        form.add(filteredResourcesWebAddon.getSettingsProvisioningBorder("settingsProvisioning", form, contentTextArea,
                saveToFileName));

        final AjaxSettingsDownloadBehavior ajaxSettingsDownloadBehavior =
                new AjaxSettingsDownloadBehavior(saveToFileName) {

                    @Override
                    protected StringResourceStream getResourceStream() {
                        FilteredResourcesWebAddon filteredResourcesWebAddon = addonsManager.addonByType(
                                FilteredResourcesWebAddon.class);
                        try {
                            String filtered = filteredResourcesWebAddon.filterResource(null,
                                    (Properties) InfoFactoryHolder.get().createProperties(),
                                    new StringReader(contentTextArea.getModelObject()));
                            return new StringResourceStream(filtered, settings.getSettingsMimeType());
                        } catch (Exception e) {
                            log.error("Unable to filter settings: " + e.getMessage());
                            return new StringResourceStream(contentTextArea.getModelObject(),
                                    settings.getSettingsMimeType());
                        }
                    }
                };

        form.add(ajaxSettingsDownloadBehavior);

        Component exportLink = new TitledAjaxSubmitLink("export", settings.getDownloadButtonTitle(), form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                ajaxSettingsDownloadBehavior.initiate(target);
            }
        };
        exportLink.add(new CssClass(new DefaultButtonStyleModel(exportLink)));
        add(exportLink);

        setWidth(700);
        add(new ModalCloseLink("cancel", "Cancel"));

        addContentToBorder();
    }

    @Override
    protected Component getContent() {
        return contentTextArea;
    }

    @Override
    protected void addContentToBorder() {
        border.add(contentTextArea);
    }
}
