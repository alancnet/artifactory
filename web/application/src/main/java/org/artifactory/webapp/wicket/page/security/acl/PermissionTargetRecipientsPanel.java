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

import org.apache.wicket.markup.html.panel.Panel;
import org.artifactory.security.PermissionTargetInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays the permissions recipients table.
 * <p/>
 *
 * @author Yoav Landman
 */
public class PermissionTargetRecipientsPanel extends Panel {
    private static final Logger log = LoggerFactory.getLogger(PermissionTargetRecipientsPanel.class);


    public PermissionTargetRecipientsPanel(String id, PermissionTargetInfo permissionTarget) {
        super(id);
        setOutputMarkupId(true);

    }


}
