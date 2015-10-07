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

package org.artifactory.webapp.wicket.page.home.settings;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.maven.MavenService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.label.highlighter.Syntax;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.webapp.wicket.page.home.settings.modal.DownloadModalSettings;
import org.artifactory.webapp.wicket.page.home.settings.modal.ReadOnlySettingsModalPanel;
import org.artifactory.webapp.wicket.page.home.settings.modal.editable.EditableSettingsModalPanel;

/**
 * A base implementation of a settings generator panel
 *
 * @author Noam Y. Tenne
 */
public abstract class BaseSettingsGeneratorPanel extends TitledPanel implements SettingsGenerator {

    @SpringBean
    protected AddonsManager addonsManager;

    @SpringBean
    protected AuthorizationService authorizationService;

    @SpringBean
    protected MavenService mavenService;

    protected Form form;
    protected String servletContextUrl;

    /**
     * Main constructor
     *
     * @param id                ID to assign to the panel
     * @param servletContextUrl Running context URL
     */
    protected BaseSettingsGeneratorPanel(String id, String servletContextUrl) {
        super(id);
        this.servletContextUrl = servletContextUrl;
        form = new SecureForm("form");
    }

    /**
     * Returns the settings generation button
     *
     * @return Generate Settings submit button
     */
    protected TitledAjaxSubmitLink getGenerateButton() {
        return new TitledAjaxSubmitLink("generate", getGenerateButtonTitle(), form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                ModalWindow modelWindow = ModalHandler.getInstanceFor(this);
                BaseModalPanel modalPanelReadOnly;
                DownloadModalSettings settings = new DownloadModalSettings(generateSettings(),
                        getSettingsMimeType(), getSaveToFileName(), getSettingsSyntax(), getDownloadButtonTitle());
                if (authorizationService.isAdmin()) {
                    modalPanelReadOnly = new EditableSettingsModalPanel(settings);
                } else {
                    modalPanelReadOnly = new ReadOnlySettingsModalPanel(settings);
                }
                modalPanelReadOnly.setTitle(getSettingsWindowTitle());
                modelWindow.setContent(modalPanelReadOnly);
                modelWindow.show(target);
            }
        };
    }

    /**
     * Returns the title of the setting generation button
     *
     * @return Generator button label
     */
    protected abstract String getGenerateButtonTitle();

    /**
     * Returns the title of the setting modal window
     *
     * @return Settings modal window title
     */
    protected abstract String getSettingsWindowTitle();

    /**
     * Returns the syntax type of the settings content
     *
     * @return Settings syntax type
     */
    protected abstract Syntax getSettingsSyntax();

    /**
     * Returns the mimetype of the settings content
     *
     * @return Settings mimetype
     */
    protected abstract String getSettingsMimeType();

    /**
     * Returns the default name to give to the file should the user want to download the settings
     *
     * @return Downloadable settings file name
     */
    protected abstract String getSaveToFileName();

    /**
     * Returns the title of the settings generation download button
     *
     * @return Download settings button title
     */
    protected abstract String getDownloadButtonTitle();
}