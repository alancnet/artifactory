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

package org.artifactory.webapp.wicket.page.security.login;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.webapp.wicket.application.ArtifactoryApplication;
import org.artifactory.webapp.wicket.page.base.BasePage;

import java.io.Serializable;

/**
 * @author Yoav Landman
 */
public class LoginPage extends BasePage implements Serializable {
    private static final long serialVersionUID = 1L;

    public LoginPage() {
        TitledBorder border = new TitledBorder("loginBorder", "outer-border");
        add(border);

        final Form form = new SecureForm<>("loginForm", new CompoundPropertyModel<>(new LoginInfo()));
        border.add(form);

        form.add(new LoginPanel("loginPanel", form));
    }

    @Override
    public String getPageName() {
        return "Log In";
    }

    @Override
    protected Class<? extends BasePage> getMenuPageClass() {
        return ArtifactoryApplication.get().getHomePage();
    }
}
