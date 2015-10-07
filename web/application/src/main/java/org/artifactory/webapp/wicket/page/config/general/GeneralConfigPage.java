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

package org.artifactory.webapp.wicket.page.config.general;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.panel.upload.HideUploadProgressDecorator;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.descriptor.addon.AddonSettings;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.webapp.wicket.application.ArtifactoryApplication;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;
import org.artifactory.webapp.wicket.page.config.general.addon.AddonSettingsPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Security configuration page.
 *
 * @author Yossi Shaul
 */
@AuthorizeInstantiation(AuthorizationService.ROLE_ADMIN)
public class GeneralConfigPage extends AuthenticatedPage {
    private static final Logger log = LoggerFactory.getLogger(GeneralConfigPage.class);

    @SpringBean
    private AddonsManager addonsManager;

    @SpringBean
    private CentralConfigService centralConfigService;

    private BaseCustomizingPanel lookAndFeelPanel;

    public GeneralConfigPage() {
        MutableCentralConfigDescriptor descriptor = centralConfigService.getMutableDescriptor();
        setDefaultModel(new CompoundPropertyModel(descriptor));

        Form form = new SecureForm("form", getDefaultModel());
        add(form);

        form.add(new AddonSettingsPanel("addonsSettingsPanel"));
        form.add(new GeneralSettingsPanel("generalConfigPanel"));

        // lnf panel
        lookAndFeelPanel = new CustomizingPanel("customizingPanel", getDefaultModel());
        form.add(lookAndFeelPanel);

        // buttons
        add(new SaveLink("save", form));
        add(new CancelLink("cancel"));
    }

    @Override
    public String getPageName() {
        return "General Configuration";
    }

    private MutableCentralConfigDescriptor getDescriptor() {
        return (MutableCentralConfigDescriptor) getDefaultModelObject();
    }

    private class SaveLink extends TitledAjaxSubmitLink {
        public SaveLink(String id, Form form) {
            super(id, "Save", form);
            form.add(new DefaultButtonBehavior(this));
        }

        @Override
        protected IAjaxCallDecorator getAjaxCallDecorator() {
            return new HideUploadProgressDecorator();
        }

        @Override
        protected void onSubmit(AjaxRequestTarget target, Form form) {
            MutableCentralConfigDescriptor descriptor = getDescriptor();

            final File logoFile = lookAndFeelPanel.getUploadedFile();
            if (lookAndFeelPanel.shouldDeleteLogo()) {
                removeUploadedLogo();
            }
            if (logoFile != null && StringUtils.isBlank(descriptor.getLogo())) {
                saveUploadedLogo(logoFile);
            }

            lookAndFeelPanel.cleanup();
            ArtifactoryApplication.get().updateLogo();

            AddonSettings oldSettings = centralConfigService.getDescriptor().getAddons();
            AddonSettings newSettings = descriptor.getAddons();

            // override addon cookie if the admin set ShowAddonInfo to true
            if (!oldSettings.isShowAddonsInfo() && newSettings.isShowAddonsInfo()) {
                newSettings.setShowAddonsInfoCookie(Long.toString(System.currentTimeMillis()));
            }

            // save changes
            centralConfigService.saveEditedDescriptorAndReload(descriptor);

            // update page model
            GeneralConfigPage.this.setDefaultModelObject(centralConfigService.getMutableDescriptor());

            // refresh ui
            final Page page = getPage();
            target.add(page.get("logo"));
            target.add(page.get("footer"));

            info("Successfully updated settings");
            AjaxUtils.refreshFeedback(target);
        }

        private void saveUploadedLogo(File logoFile) {
            try {
                centralConfigService.setLogo(logoFile);
            } catch (Exception e) {
                String errorMessage = "Could not save uploaded logo";
                log.error(errorMessage, e);
                error(errorMessage);
            }
        }

        private void removeUploadedLogo() {
            try {
                centralConfigService.setLogo(null);
            } catch (IOException e) {
                String errorMessage = "Could not remove logo";
                log.error(errorMessage, e);
                error(errorMessage);
            }
        }

    }

    private class CancelLink extends TitledAjaxLink {
        private CancelLink(String id) {
            super(id, "Cancel");
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            lookAndFeelPanel.cleanup();
            setResponsePage(GeneralConfigPage.class);
        }
    }
}