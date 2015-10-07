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

package org.artifactory.webapp.wicket.page.config.layout.validators;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;

import java.util.regex.Pattern;

/**
 * Verifies that the repository pattern fields contain all the required tokens
 *
 * @author Noam Y. Tenne
 */
public class LayoutFieldRequiredTokenValidator extends StringValidator {

    private static String[] requiredTokens = new String[]{"(org|orgPath)", "module", "baseRev"};

    @Override
    protected void onValidate(IValidatable<String> validatable) {
        String value = validatable.getValue();
        if (StringUtils.isNotBlank(value)) {
            for (String requiredToken : requiredTokens) {
                if (!Pattern.compile("\\[" + requiredToken + "\\]").matcher(value).find()) {
                    ValidationError error = new ValidationError();
                    error.setMessage("Pattern '" + value + "' must at-least contain the tokens 'module', " +
                            "'baseRev' and 'org' or 'orgPath'.");
                    validatable.error(error);
                    return;
                }
            }
        }
    }
}
