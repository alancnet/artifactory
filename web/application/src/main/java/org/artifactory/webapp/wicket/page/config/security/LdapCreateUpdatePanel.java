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

package org.artifactory.webapp.wicket.page.config.security;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.LdapGroupWebAddon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.StatusEntry;
import org.artifactory.common.StatusEntryLevel;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.common.wicket.component.CreateUpdatePanel;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.modal.links.ModalCloseLink;
import org.artifactory.common.wicket.component.panel.feedback.UnescapedFeedbackMessage;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.descriptor.security.ldap.SearchPattern;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.page.config.SchemaHelpModel;
import org.artifactory.webapp.wicket.page.logs.SystemLogsPage;
import org.artifactory.webapp.wicket.util.validation.UniqueXmlIdValidator;
import org.artifactory.webapp.wicket.util.validation.UriValidator;
import org.artifactory.webapp.wicket.util.validation.XsdNCNameValidator;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Ldaps configuration panel.
 *
 * @author Yossi Shaul
 */
public class LdapCreateUpdatePanel extends CreateUpdatePanel<LdapSetting> {
    @SpringBean
    private CentralConfigService centralConfigService;

    @SpringBean
    private AddonsManager addonsManager;

    @SpringBean
    private SecurityService securityService;

    SearchPattern searchPattern;

    @WicketProperty
    private String testUsername;

    @WicketProperty
    private String testPassword;
    private LdapSetting originalDescriptor;

    public LdapCreateUpdatePanel(CreateUpdateAction action, LdapSetting ldapDescriptor) {
        /**
         * Creating a local copy of the original descriptor so that we can fool around with it without changing the
         * original until we need to save
         */
        super(action, new LdapSetting(ldapDescriptor));
        originalDescriptor = ldapDescriptor;
        setWidth(500);

        add(form);

        TitledBorder border = new TitledBorder("border");
        form.add(border);

        // Ldap key
        RequiredTextField<String> ldapKeyField = new RequiredTextField<>("key");
        setDefaultFocusField(ldapKeyField);
        ldapKeyField.setEnabled(isCreate());// don't allow key update
        if (isCreate()) {
            ldapKeyField.add(new XsdNCNameValidator("Invalid LDAP key '%s'"));
            ldapKeyField.add(new UniqueXmlIdValidator(centralConfigService.getMutableDescriptor()));
        }
        border.add(ldapKeyField);
        border.add(new SchemaHelpBubble("key.help"));

        border.add(new StyledCheckbox("enabled"));

        TextField<String> ldapUrlField = new RequiredTextField<>("ldapUrl");
        ldapUrlField.add(new UriValidator("ldap", "ldaps"));
        border.add(ldapUrlField);
        border.add(new SchemaHelpBubble("ldapUrl.help"));

        TitledBorder borderDn = new TitledBorder("borderDn");
        form.add(borderDn);
        borderDn.add(new TextField("userDnPattern"));
        borderDn.add(new TextField("emailAttribute"));
        borderDn.add(new SchemaHelpBubble("emailAttribute.help"));
        borderDn.add(new StyledCheckbox("autoCreateUser"));
        borderDn.add(new SchemaHelpBubble("autoCreateUser.help"));
        borderDn.add(new SchemaHelpBubble("userDnPattern.help"));

        addSearchFields(borderDn);

        addTestConnectionFields();

        // Cancel button
        form.add(new ModalCloseLink("cancel"));

        // Submit button
        TitledAjaxSubmitLink submitButton = createSubmitButton();
        form.add(submitButton);
        form.add(new DefaultButtonBehavior(submitButton));

        add(form);
    }

    private void addSearchFields(TitledBorder borderDn) {
        searchPattern = entity.getSearch();
        if (searchPattern == null) {
            searchPattern = new SearchPattern();
        }

        borderDn.add(new TextField<>("searchFilter", new PropertyModel<String>(searchPattern, "searchFilter")));
        borderDn.add(new SchemaHelpBubble("searchFilter.help", new SchemaHelpModel(searchPattern, "searchFilter")));

        borderDn.add(new TextField<>("searchBase", new PropertyModel<String>(searchPattern, "searchBase")));
        borderDn.add(new SchemaHelpBubble("searchBase.help", new SchemaHelpModel(searchPattern, "searchBase")));

        borderDn.add(new StyledCheckbox("searchSubTree", new PropertyModel<Boolean>(searchPattern, "searchSubTree")));
        borderDn.add(new SchemaHelpBubble("searchSubTree.help", new SchemaHelpModel(searchPattern, "searchSubTree")));

        borderDn.add(new TextField<>("managerDn", new PropertyModel<String>(searchPattern, "managerDn")));
        borderDn.add(new SchemaHelpBubble("managerDn.help", new SchemaHelpModel(searchPattern, "managerDn")));

        PasswordTextField managerPasswordField = new PasswordTextField(
                "managerPassword", new PropertyModel<String>(searchPattern, "managerPassword"));
        managerPasswordField.setRequired(false);
        managerPasswordField.setResetPassword(false);
        borderDn.add(managerPasswordField);
        borderDn.add(new SchemaHelpBubble("managerPassword.help",
                new SchemaHelpModel(searchPattern, "managerPassword")));
    }

    private void addTestConnectionFields() {
        TitledBorder borderTest = new TitledBorder("borderTest");
        form.add(borderTest);
        borderTest.add(new TextField<>("testUsername", new PropertyModel<String>(this, "testUsername")));
        borderTest.add(new HelpBubble("testUsername.help", "Username to test the LDAP connection"));

        PasswordTextField testPasswordField = new PasswordTextField(
                "testPassword", new PropertyModel<String>(this, "testPassword"));
        testPasswordField.setRequired(false);
        testPasswordField.setResetPassword(false);
        borderTest.add(testPasswordField);
        borderTest.add(new HelpBubble("testPassword.help", "Password to test the LDAP connection"));

        // Test connection button
        borderTest.add(createTestConnectionButton());
    }

    private TitledAjaxSubmitLink createSubmitButton() {
        String submitCaption = isCreate() ? "Create" : "Save";
        TitledAjaxSubmitLink submit = new TitledAjaxSubmitLink("submit", submitCaption, form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                Session.get().cleanupFeedbackMessages();
                if (!validateAndUpdateLdapSettings()) {
                    AjaxUtils.refreshFeedback(target);
                    return;
                }

                MutableCentralConfigDescriptor configDescriptor = centralConfigService.getMutableDescriptor();
                originalDescriptor.duplicate(entity);
                if (isCreate()) {
                    configDescriptor.getSecurity().addLdap(originalDescriptor);
                    centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
                    getPage().info("Successfully created LDAP '" + originalDescriptor.getKey() + "'");
                    ((LdapsListPage) getPage()).refresh(target);
                } else {
                    LdapGroupWebAddon addon = addonsManager.addonByType(LdapGroupWebAddon.class);
                    addon.saveLdapSetting(configDescriptor, originalDescriptor);
                    centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
                    getPage().info("Successfully created LDAP '" + originalDescriptor.getKey() + "'");
                    ((LdapsListPage) getPage()).refresh(target);
                }
                AjaxUtils.refreshFeedback(target);
                ModalHandler.closeCurrent(target);
            }
        };
        return submit;
    }

    private TitledAjaxSubmitLink createTestConnectionButton() {
        TitledAjaxSubmitLink submit = new TitledAjaxSubmitLink("testLdap", "Test Connection", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                Session.get().cleanupFeedbackMessages();
                if (!validateAndUpdateLdapSettings()) {
                    AjaxUtils.refreshFeedback(target);
                    return;
                }

                if (!StringUtils.hasText(testUsername) || !StringUtils.hasText(testPassword)) {
                    error("Please enter test username and password to test the LDAP settings");
                    AjaxUtils.refreshFeedback(target);
                    return;
                }
                BasicStatusHolder status = securityService.testLdapConnection(entity, testUsername, testPassword);
                List<StatusEntry> infos = status.getEntries(StatusEntryLevel.INFO);
                if (status.isError()) {
                    String systemLogsPage = WicketUtils.absoluteMountPathForPage(SystemLogsPage.class);
                    error(new UnescapedFeedbackMessage(
                            status.getStatusMsg() + " Please see the <a href=\"" + systemLogsPage +
                                    "\">logs</a> page for more details"
                    ));
                }
                for (StatusEntry info : infos) {
                    info(info.getMessage());
                }
                List<StatusEntry> warnings = status.getEntries(StatusEntryLevel.WARNING);
                for (StatusEntry warning : warnings) {
                    warn(warning.getMessage());
                }
                AjaxUtils.refreshFeedback(target);
            }
        };
        return submit;
    }

    private boolean validateAndUpdateLdapSettings() {
        // validate userDnPattern or searchFilter
        boolean hasDnPattern = StringUtils.hasText(entity.getUserDnPattern());
        boolean hasSearch = StringUtils.hasText(searchPattern.getSearchFilter());
        if (!hasDnPattern && !hasSearch) {
            error("LDAP settings should provide a userDnPattern or a searchFilter (or both)");
            return false;
        }
        if (searchPattern.getSearchBase() == null) {
            searchPattern.setSearchBase("");
        }
        if (searchPattern.getSearchFilter() == null) {
            searchPattern.setSearchFilter("");
        }

        // if the search filter has value set the search pattern
        String managerPassword = searchPattern.getManagerPassword();
        if (StringUtils.hasText(searchPattern.getSearchFilter()) || (StringUtils.hasText(
                searchPattern.getManagerDn()) && StringUtils.hasText(managerPassword))) {
            // Encrypt password before updating the entity
            searchPattern.setManagerPassword(CryptoHelper.encryptIfNeeded(managerPassword));
            entity.setSearch(searchPattern);
        }

        return true;
    }


}