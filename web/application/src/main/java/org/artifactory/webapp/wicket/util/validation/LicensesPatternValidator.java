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

package org.artifactory.webapp.wicket.util.validation;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.PatternValidator;

import java.util.regex.Pattern;

/**
 * Validate that the license name is a valid one, and does not contain illegal characters (e.g. %,^,*)
 *
 * @author Tomer Cohen
 */
public class LicensesPatternValidator extends PatternValidator {

    private static final Pattern PATTERN = Pattern.compile("^[A-Za-z0-9\\._-]*$");

    public LicensesPatternValidator() {
        super(PATTERN);
    }

    @Override
    protected void onValidate(IValidatable<String> validatable) {
        if (!PATTERN.matcher(validatable.getValue()).matches()) {
            ValidationError error = new ValidationError();
            error.setMessage("License name contains illegal characters");
            validatable.error(error);
        }
    }
}
