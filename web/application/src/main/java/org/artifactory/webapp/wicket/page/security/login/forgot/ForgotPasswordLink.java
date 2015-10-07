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

package org.artifactory.webapp.wicket.page.security.login.forgot;

import org.apache.wicket.markup.ComponentTag;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.links.TitledLink;
import org.artifactory.common.wicket.util.WicketUtils;

/**
 * Forgot password link button.
 *
 * @author Tomer Cohen
 */
public class ForgotPasswordLink extends TitledLink {
    public ForgotPasswordLink(String id) {
        super(id, "Forgot Your Password?");
        add(new CssClass("forgot-password"));
    }

    @Override
    public void onClick() {
        setResponsePage(ForgotPasswordPage.class);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.setName("a");
        tag.put("href", WicketUtils.absoluteMountPathForPage(ForgotPasswordPage.class));
    }
}
