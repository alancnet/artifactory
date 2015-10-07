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

package org.artifactory.webapp.wicket.page.security.acl;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.artifactory.api.security.AclService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.security.MutablePermissionTargetInfo;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;

@AuthorizeInstantiation(AuthorizationService.ROLE_USER)
public class AclsPage extends AuthenticatedPage {

    @SpringBean
    private AclService aclService;

    @SpringBean
    private AuthorizationService authService;

    public AclsPage() {
        add(new PermissionTargetListPanel("permissionTargetList"));
    }

    /**
     * Creates an acls page and opens the permission target for editing.
     *
     * @param ptiToEdit Permission target to edit
     */
    public AclsPage(final MutablePermissionTargetInfo ptiToEdit) {
        // only admins can reach here
        if (!authService.isAdmin()) {
            throw new UnauthorizedInstantiationException(AclsPage.class);
        }
        // create the panel
        final PermissionTargetListPanel panel = new PermissionTargetListPanel("permissionTargetList");
        add(panel);

        if (ptiToEdit != null) {
            // use very short ajax timer to open the edit panel
            add(new AbstractAjaxTimerBehavior(Duration.milliseconds(1)) {
                @Override
                protected void onTimer(AjaxRequestTarget target) {
                    stop(); // don't fire again
                    ModalHandler modalHandler = ModalHandler.getInstanceFor(AclsPage.this);
                    modalHandler.setModalPanel(panel.newUpdateItemPanel(ptiToEdit));
                    modalHandler.show(target);
                }
            });
        }
    }

    @Override
    public String getPageName() {
        return "Permissions Management";
    }

}