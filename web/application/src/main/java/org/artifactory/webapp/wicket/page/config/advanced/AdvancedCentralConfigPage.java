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

package org.artifactory.webapp.wicket.page.config.advanced;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.panel.editor.TextEditorPanel;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.security.AccessLogger;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enables the user to edit the central configuration configuration descriptor as an XML file
 *
 * @author Noam Tenne
 */
@AuthorizeInstantiation(AuthorizationService.ROLE_ADMIN)
public class AdvancedCentralConfigPage extends AuthenticatedPage {

    private static final Logger log = LoggerFactory.getLogger(AdvancedCentralConfigPage.class);

    @SpringBean
    private CentralConfigService centralConfigService;

    public AdvancedCentralConfigPage() {
        addCentralConfigPanel();
    }

    /**
     * Add the central configuration editor panel
     */
    private void addCentralConfigPanel() {
        Form centralForm = new SecureForm("centralForm");
        // Set the form as multi part since the config can be quite big and jetty for example
        // doesn't support more than 200K POST data, see RTFACT-4931
        centralForm.setMultiPart(true);
        add(centralForm);

        final TextEditorPanel centralPanel = new TextEditorPanel("centralPanel", "Central Configuration Descriptor",
                "Updating the configuration through direct manipulation of the XML descriptors is an advanced " +
                        "feature. You should use this feature only if you know what you are doing.");
        TitledAjaxSubmitLink saveCentralButton = new TitledAjaxSubmitLink("centralSave", "Save", centralForm) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                String editorValue = centralPanel.getEditorValue();
                if (StringUtils.isEmpty(editorValue)) {
                    error("Cannot save null or empty central configuration.");
                } else if (ContextHelper.get().isOffline()) {
                    error("Cannot save config descriptor during offline state.");
                } else {
                    try {
                        centralConfigService.setConfigXml(editorValue,true);
                        AccessLogger.configurationChanged();
                        info("Central configuration successfully saved.");
                    } catch (Exception e) {
                        log.debug("Error while manually saving the central configuration.", e);
                        error("An error has occurred while saving the central configuration (" + e.getMessage() +
                                "). Please verify the validity of your input.");
                    }
                }
                AjaxUtils.refreshFeedback(target);
            }
        };
        centralForm.add(centralPanel);
        String configXml = centralConfigService.getConfigXml();
        centralPanel.setEditorValue(configXml);
        centralForm.add(saveCentralButton);
        TitledAjaxLink cancelCentralButton = new TitledAjaxLink("centralCancel", "Cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(AdvancedCentralConfigPage.class);
            }
        };
        centralForm.add(cancelCentralButton);
        centralForm.add(new DefaultButtonBehavior(saveCentralButton));
    }

    @Override
    public String getPageName() {
        return "Central Configuration";
    }
}
