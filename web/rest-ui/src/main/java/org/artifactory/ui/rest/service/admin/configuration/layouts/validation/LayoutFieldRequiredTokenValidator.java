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

package org.artifactory.ui.rest.service.admin.configuration.layouts.validation;

import org.apache.commons.lang.StringUtils;
import org.artifactory.ui.rest.service.utils.validation.ValidationException;

import java.util.regex.Pattern;

/**
 * Verifies that the repository pattern fields contain all the required tokens
 *
 * @author Lior Hasson
 */
public class LayoutFieldRequiredTokenValidator {
    private static String[] requiredTokens = new String[]{"(org|orgPath)", "module", "baseRev"};

    public static void onValidate(String validatable) throws ValidationException{
        if (StringUtils.isNotBlank(validatable)) {
            for (String requiredToken : requiredTokens) {
                if (!Pattern.compile("\\[" + requiredToken + "\\]").matcher(validatable).find()) {
                    throw new ValidationException("Pattern '" + validatable +
                            "' must at-least contain the tokens 'module', " +
                            "'baseRev' and 'org' or 'orgPath'.");
                }
            }
        }
    }
}
