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

package org.artifactory.common.wicket.component.panel.passwordstrength;

import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.component.ProgressComponent;

/**
 * @author Eli Givoni
 */
public class PasswordStrengthComponent extends ProgressComponent {
    public PasswordStrengthComponent(String id, IModel model) {
        super(id, model);
    }

    @Override
    protected String generateNewAttributeString(Object num, Object tpassword) {
        String password = (String) tpassword;
        if (password != null) {
            double percent = (Double) num * 16.666;
            return "width:" + percent + "%";
        }
        return "width:0%";
    }


    @Override
    protected double getProgress(Object password) {
        String strPassword = (String) password;
        int score = 0;
        //if password bigger than 6 give 1 point
        if (strPassword == null) {
            return score;
        }

        if (strPassword.length() > 6) {
            score++;
        }

        String strCheck = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        //if password has both lower and uppercase characters give 1 point

        if (doesContain(strPassword, strCheck)) {
            score++;
        }

        if (doesContain(strPassword, strCheck.toLowerCase())) {
            score++;
        }


        //if password has at least one number give 1 point
        strCheck = "0123456789";
        if (doesContain(strPassword, strCheck)) {
            score++;
        }


        //if password has at least one special caracther give 1 point
        strCheck = ";:-=+\\|//?^&!.@$#&*()%~<>{}[]";
        if (doesContain(strPassword, strCheck)) {
            score++;
        }


        //if password bigger than 12 give another 1 point

        if (strPassword.length() > 12) {
            score++;
        }

        return score;

    }


    private boolean doesContain(String password, String check) {
        for (int i = 0; i < password.length(); i++) {
            if (check.indexOf(password.charAt(i)) > -1) {
                return true;
            }
        }
        return false;
    }
}
