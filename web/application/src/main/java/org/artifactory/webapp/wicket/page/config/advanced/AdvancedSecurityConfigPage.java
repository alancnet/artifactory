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

import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.panel.editor.TextEditorPanel;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.SecurityInfo;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enables the user to edit the security configuration descriptor as an XML file
 *
 * @author Noam Tenne
 */
@AuthorizeInstantiation(AuthorizationService.ROLE_ADMIN)
public class AdvancedSecurityConfigPage extends AuthenticatedPage {

    private static final Logger log = LoggerFactory.getLogger(AdvancedSecurityConfigPage.class);

    @SpringBean
    private SecurityService securityService;

    public AdvancedSecurityConfigPage() {
        addSecurityConfigPanel();
    }

    /**
     * Add the security configuration editor panel
     */
    private void addSecurityConfigPanel() {
        Form securityForm = new SecureForm("securityForm");
        add(securityForm);
        final TextEditorPanel securityPanel = new TextEditorPanel("securityPanel", "Security Configuration Descriptor",
                "Updating the configuration through direct manipulation of the XML descriptors is an advanced " +
                        "feature. You should use this feature only if you know what you are doing.");
        TitledAjaxSubmitLink saveSecurityButton = new TitledAjaxSubmitLink("securitySave", "Save", securityForm) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                String editorValue = securityPanel.getEditorValue();
                if (StringUtils.isEmpty(editorValue)) {
                    error("Cannot save null or empty security configuration.");
                } else {
                    try {
                        securityService.importSecurityData(editorValue);
                        info("Security configuration successfully saved.");
                    } catch (Exception e) {
                        log.debug("Error while manually saving the security configuration.", e);
                        error("An error has occurred while saving the security configuration (" + e.getMessage() +
                                "). Please verify the validity of your input.");
                    }
                }
                AjaxUtils.refreshFeedback(target);
            }
        };
        securityForm.add(securityPanel);
        SecurityInfo securityData = securityService.getSecurityData();
        XStream xstream = InfoFactoryHolder.get().getSecurityXStream();
        securityPanel.setEditorValue(xstream.toXML(securityData));
        securityForm.add(saveSecurityButton);
        TitledAjaxLink cancelSecurityButton = new TitledAjaxLink("securityCancel", "Cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(AdvancedSecurityConfigPage.class);
            }
        };
        securityForm.add(cancelSecurityButton);
        securityForm.add(new DefaultButtonBehavior(saveSecurityButton));
    }

    @Override
    public String getPageName() {
        return "Security Configuration";
    }
}