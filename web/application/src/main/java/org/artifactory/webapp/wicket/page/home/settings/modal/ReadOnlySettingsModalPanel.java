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

package org.artifactory.webapp.wicket.page.home.settings.modal;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.StringResourceStream;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.FilteredResourcesWebAddon;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonStyleModel;
import org.artifactory.common.wicket.component.label.highlighter.SyntaxHighlighter;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.modal.links.ModalCloseLink;
import org.artifactory.common.wicket.component.modal.panel.bordered.nesting.CodeModalPanel;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.webapp.wicket.page.home.settings.modal.download.AjaxSettingsDownloadBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

/**
 * A custom modal display panel for the generated settings. Created for the need of customizing the modal window
 * content panel, so an export link could be added
 *
 * @author Noam Y. Tenne
 */
public class ReadOnlySettingsModalPanel extends CodeModalPanel {

    private static final Logger log = LoggerFactory.getLogger(ReadOnlySettingsModalPanel.class);

    @SpringBean
    private AddonsManager addonsManager;

    public ReadOnlySettingsModalPanel(final DownloadModalSettings settings) {
        super(new SyntaxHighlighter("content", settings.getContent(), settings.getSyntax()));

        final AjaxSettingsDownloadBehavior ajaxSettingsDownloadBehavior =
                new AjaxSettingsDownloadBehavior(settings.getSaveToFileName()) {

                    @Override
                    protected StringResourceStream getResourceStream() {
                        FilteredResourcesWebAddon filteredResourcesWebAddon = addonsManager.addonByType(
                                FilteredResourcesWebAddon.class);
                        String settingsMimeType = settings.getSettingsMimeType();
                        String settingsContent = settings.getContent();
                        try {
                            String filtered = filteredResourcesWebAddon.filterResource(null,
                                    (Properties) InfoFactoryHolder.get().createProperties(),
                                    new StringReader(settingsContent));
                            return new StringResourceStream(filtered, settingsMimeType);
                        } catch (Exception e) {
                            log.error("Unable to filter settings: " + e.getMessage());
                            return new StringResourceStream(settingsContent, settingsMimeType);
                        }
                    }
                };

        add(ajaxSettingsDownloadBehavior);

        Component exportLink = new TitledAjaxLink("export", settings.getDownloadButtonTitle()) {
            public void onClick(AjaxRequestTarget target) {
                ajaxSettingsDownloadBehavior.initiate(target);
            }
        };
        exportLink.add(new CssClass(new DefaultButtonStyleModel(exportLink)));
        add(exportLink);

        setWidth(700);
        add(new ModalCloseLink("cancel", "Cancel"));
    }
}