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

package org.artifactory.webapp.wicket.page.config.security.general;

import org.apache.commons.lang.WordUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.MasterEncryptionService;
import org.artifactory.common.wicket.component.CancelLink;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.EncryptionPolicy;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static java.lang.String.format;

/**
 * Security general configuration page.
 *
 * @author Yossi Shaul
 */
@AuthorizeInstantiation(AuthorizationService.ROLE_ADMIN)
public class SecurityGeneralConfigPage extends AuthenticatedPage {
    private static final Logger log = LoggerFactory.getLogger(SecurityGeneralConfigPage.class);

    @SpringBean
    private CentralConfigService centralConfigService;

    @SpringBean
    private MasterEncryptionService masterEncryptionService;
    private TitledBorder encryptBorder;
    private TitledBorder decryptBorder;
    private StyledCheckbox anonAccess;
    private StyledCheckbox anonBuildAccess;

    public SecurityGeneralConfigPage() {
        TitledBorder generalBorder = new TitledBorder("generalBorder");
        add(generalBorder);

        MutableCentralConfigDescriptor centralConfig = centralConfigService.getMutableDescriptor();
        SecurityDescriptor securityDescriptor = centralConfig.getSecurity();
        CompoundPropertyModel<SecurityDescriptor> securityModel = new CompoundPropertyModel<>(securityDescriptor);
        Form<SecurityDescriptor> form = new SecureForm<>("form", securityModel);
        generalBorder.add(form);

        setOutputMarkupId(true);
        addAnonymousAccessField(form);
        addAnonymousAccessToBuildInfosField(form);
        addHideUnauthorizedResourcesField(form);
        addEncryptionPolicyDropDown(form);
        createButtons(form);
        addEncryptBorder();
        addDecryptBorder();
    }

    private void addAnonymousAccessField(Form form) {
        anonAccess = new StyledCheckbox("anonAccessEnabled");
        anonAccess.setLabel(Model.of("Allow Anonymous Access"));
        form.add(anonAccess);
        form.add(new SchemaHelpBubble("anonAccessEnabled.help"));
        anonAccess.add(new AjaxFormComponentUpdatingBehavior("onclick") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (anonAccess.getModelObject()){
                    anonBuildAccess.setEnabled(true);
                }
                else{
                    anonBuildAccess.setModelObject(false);
                    anonBuildAccess.setEnabled(false);
                }
                if (target != null) {
                    target.add(anonBuildAccess);
                }
            }
        });
    }

    private void addAnonymousAccessToBuildInfosField(Form form) {
        anonBuildAccess = new StyledCheckbox("anonAccessToBuildInfosDisabled");
        anonBuildAccess.setLabel(Model.of("Prevent Anonymous Access to Build Related Info"));
        anonBuildAccess.setOutputMarkupId(true);
        if(!anonAccess.getModelObject()) {
            anonBuildAccess.setEnabled(false);
        }

        form.add(anonBuildAccess);
        form.add(new SchemaHelpBubble("anonAccessToBuildInfosDisabled.help"));
    }

    private void addHideUnauthorizedResourcesField(Form form) {
        StyledCheckbox anonAccess = new StyledCheckbox("hideUnauthorizedResources");
        anonAccess.setLabel(Model.of("Hide Existence of Unauthorized Resources"));
        form.add(anonAccess);
        form.add(new SchemaHelpBubble("hideUnauthorizedResources.help"));
    }

    private void addEncryptionPolicyDropDown(Form form) {
        EncryptionPolicy[] encryptionPolicies = EncryptionPolicy.values();
        DropDownChoice<EncryptionPolicy> encryptionPoliciesDC = new DropDownChoice<>(
                "passwordSettings.encryptionPolicy",
                Arrays.asList(encryptionPolicies));
        encryptionPoliciesDC.setChoiceRenderer(new EncryptionPolicyChoiceRenderer());
        form.add(encryptionPoliciesDC);
        form.add(new SchemaHelpBubble("passwordSettings.encryptionPolicy.help", "passwordSettings.encryptionPolicy"));
    }

    private void addEncryptBorder() {
        encryptBorder = new TitledBorder("encryptBorder");
        encryptBorder.setOutputMarkupPlaceholderTag(true);
        add(encryptBorder);

        encryptBorder.add(
                new Label("encryptLabel", "All passwords in your configuration are currently visible in plain text."));


        TitledAjaxLink encryptLink = new TitledAjaxLink("encrypt", "Encrypt") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                try {
                    masterEncryptionService.encrypt();
                    encryptBorder.setVisible(false);
                    decryptBorder.setVisible(true);
                    target.add(encryptBorder, decryptBorder);
                    info("Configuration successfully encrypted.");
                } catch (Exception e) {
                    log.error("Could not encrypt with master key, due to: " + e.getMessage(), e);
                    error("Failed to encrypt the configuration. For more details please check the logs.");
                }
                AjaxUtils.refreshFeedback(target);
            }
        };
        encryptBorder.add(encryptLink);
        HelpBubble encryptHelpBubble = new HelpBubble("encryptHelp",
                format(getString("encryptHelp"), CryptoHelper.getKeyFilePath()));
        encryptBorder.add(encryptHelpBubble);

        // Only show if the master key doesn't exist (configuration is not already encrypted)
        encryptBorder.setVisible(!CryptoHelper.hasMasterKey());
    }

    private void addDecryptBorder() {
        decryptBorder = new TitledBorder("decryptBorder");
        decryptBorder.setOutputMarkupPlaceholderTag(true);
        add(decryptBorder);

        decryptBorder.add(new Label("decryptLabel", "All passwords in your configuration are currently encrypted."));

        TitledAjaxLink decryptLink = new TitledAjaxLink("decrypt", "Decrypt") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                try {
                    masterEncryptionService.decrypt();
                    decryptBorder.setVisible(false);
                    encryptBorder.setVisible(true);
                    target.add(decryptBorder, encryptBorder);
                    info("Configuration successfully decrypted.");
                } catch (Exception e) {
                    log.error("Could not decrypt with master key, due to: " + e.getMessage(), e);
                    error("Failed to decrypt the configuration. For more details please check the logs.");
                }
                AjaxUtils.refreshFeedback(target);
            }
        };
        decryptBorder.add(decryptLink);
        HelpBubble decryptHelpBubble = new HelpBubble("decryptHelp",
                format(getString("decryptHelp"), CryptoHelper.getKeyFilePath()));
        decryptBorder.add(decryptHelpBubble);

        // Only show if the master key exists (configuration is encrypted)
        decryptBorder.setVisible(CryptoHelper.hasMasterKey());
    }

    private void createButtons(Form<SecurityDescriptor> form) {
        add(new TitledAjaxSubmitLink("saveButton", "Save", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                MutableCentralConfigDescriptor centralConfig = centralConfigService.getMutableDescriptor();
                SecurityDescriptor editedDescriptor = (SecurityDescriptor) form.getModelObject();
                centralConfig.setSecurity(editedDescriptor);
                centralConfigService.saveEditedDescriptorAndReload(centralConfig);
                info("Successfully updated security settings");
                AjaxUtils.refreshFeedback(target);
            }
        });

        add(new CancelLink("cancel", form) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(SecurityGeneralConfigPage.class);
            }
        });
    }

    @Override
    public String getPageName() {
        return "Security General Configuration";
    }

    private static class EncryptionPolicyChoiceRenderer extends ChoiceRenderer<EncryptionPolicy> {
        @Override
        public String getDisplayValue(EncryptionPolicy policy) {
            return WordUtils.capitalizeFully(policy.toString());
        }
    }
}