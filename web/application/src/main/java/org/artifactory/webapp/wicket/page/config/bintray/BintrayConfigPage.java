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
package org.artifactory.webapp.wicket.page.config.bintray;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.CancelLink;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.descriptor.bintray.BintrayConfigDescriptor;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.page.config.SchemaHelpModel;
import org.springframework.util.StringUtils;

/**
 * Holds the Global Bintray configuration panel and manages the descriptor editing
 *
 * @author Dan Feldman
 */
@AuthorizeInstantiation(AuthorizationService.ROLE_ADMIN)
public class BintrayConfigPage extends AuthenticatedPage {

    private final CancelLink cancelLink;
    private final TitledAjaxSubmitLink saveLink;
    private final Form form;
    private final RequiredTextField<Integer> fileUploadLimit;
    GlobalBintrayConfigPanel<BintrayConfigDescriptor> bintrayPanel;
    @SpringBean
    private CentralConfigService centralConfigService;

    public BintrayConfigPage() {

        setOutputMarkupId(true);
        form = new SecureForm("form");


        saveLink = createSaveButton(form);
        saveLink.setEnabled(true);
        saveLink.setVisible(true);
        add(saveLink);
        form.add(new DefaultButtonBehavior(saveLink));

        cancelLink = new CancelLink("cancel", form) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(BintrayConfigPage.class);
            }
        };
        add(cancelLink);

        bintrayPanel = new GlobalBintrayConfigPanel<>("globalBintrayPanel", getBintrayDescriptor());
        form.add(bintrayPanel);

        form.add(new SchemaHelpBubble("fileUploadLimit.help",
                new SchemaHelpModel(getBintrayDescriptor(), "fileUploadLimit")));
        fileUploadLimit = new RequiredTextField<Integer>("fileUploadLimit");
        fileUploadLimit.setOutputMarkupId(true);
        fileUploadLimit.setDefaultModel(
                new PropertyModel<Integer>(bintrayPanel.getDefaultModelObject(), "fileUploadLimit"));
        fileUploadLimit.add(new RangeValidator<>(0, Integer.MAX_VALUE));
        fileUploadLimit.setEnabled(true);
        form.add(fileUploadLimit);

        add(form);
    }


    /**
     * Creates the form save button
     *
     * @return TitledAjaxSubmitLink - The save button
     */
    public TitledAjaxSubmitLink createSaveButton(final Form form) {
        return new TitledAjaxSubmitLink("save", "Save", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                BintrayConfigDescriptor model = (BintrayConfigDescriptor) bintrayPanel.getDefaultModelObject();

                if (!StringUtils.hasText(model.getApiKey())
                        && StringUtils.hasText(model.getUserName())) {
                    error("Cannot save Bintray username without an API key.");
                } else if (!StringUtils.hasText(model.getUserName())
                        && StringUtils.hasText(model.getApiKey())) {
                    error("Cannot save Bintray API key without username.");
                } else {
                    MutableCentralConfigDescriptor cc = centralConfigService.getMutableDescriptor();
                    cc.setBintrayConfig(((BintrayConfigDescriptor) bintrayPanel.getDefaultModelObject()));
                    centralConfigService.saveEditedDescriptorAndReload(cc);
                    info("Default Bintray Credentials updated");
                }
                target.add(bintrayPanel);
                AjaxUtils.refreshFeedback(target);
            }
        };
    }

    @Override
    public String getPageName() {
        return "Configure Default Bintray Credentials";
    }

    private BintrayConfigDescriptor getBintrayDescriptor() {
        CentralConfigDescriptor centralConfig = centralConfigService.getMutableDescriptor();
        BintrayConfigDescriptor bintrayConfigDescriptor = centralConfig.getBintrayConfig();
        if (bintrayConfigDescriptor == null) {
            bintrayConfigDescriptor = new BintrayConfigDescriptor();
        }
        return bintrayConfigDescriptor;
    }
}
