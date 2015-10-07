/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.webapp.wicket.page.admin;

import org.apache.wicket.util.tester.FormTester;
import org.artifactory.webapp.wicket.page.security.login.LoginPage;
import org.testng.annotations.Test;

@Test
public class LoginPageTest extends AbstractWicketTest {

    @Override
    protected void setupTest() {
    }

    public void formSubmitWithCSRFToken() throws Exception {
        getTester().startPage(LoginPage.class);
        getTester().assertRenderedPage(LoginPage.class);

        // We expect the CSRF token to be rendered in the HTML
        getTester().assertContains("SECURE_FORM_TOKEN");

        // If the user doesn't send the token, the form should fail!
        getTester().getRequest().getPostParameters().addParameterValue("SECURE_FORM_TOKEN",
                (String) getTester().getSession().getAttribute("SECURE_FORM_TOKEN"));

        // NOTE! formTester.submit() is by-passing the onSubmit() on DefaultLoginLink so it's not a real submit!!
        FormTester formTester = getTester().newFormTester("loginBorder:loginBorder_body:loginForm");
        formTester.setValue("loginPanel:username", "admin");
        formTester.setValue("loginPanel:password", "password");
        formTester.submit();

        // This doesn't work since FormTester doesn't actually call onSubmit() so we are not transferred anywhere after successful login
        //getTester().assertRenderedPage(HomePage.class);
    }
}