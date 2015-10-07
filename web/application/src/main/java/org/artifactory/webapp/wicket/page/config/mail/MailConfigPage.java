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

package org.artifactory.webapp.wicket.page.config.mail;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;

/**
 * Holds the Mail server configuration panel and manages the descriptor editing
 *
 * @author Noam Tenne
 */
@AuthorizeInstantiation(AuthorizationService.ROLE_ADMIN)
public class MailConfigPage extends AuthenticatedPage {

    public MailConfigPage() {
        MailConfigPanel mailConfigPanel = new MailConfigPanel("mailConfigPanel");
        add(mailConfigPanel);

        TitledAjaxSubmitLink saveButton = mailConfigPanel.createSaveButton();
        add(saveButton);
        add(createCancelButton());
    }

    /**
     * Creates a cancel button for the panel
     *
     * @return TitledAjaxLink - The cancel button
     */
    private TitledAjaxLink createCancelButton() {
        return new TitledAjaxLink("cancel", "Cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(MailConfigPage.class);
            }
        };
    }

    @Override
    public String getPageName() {
        return "Configure Mail";
    }
}
