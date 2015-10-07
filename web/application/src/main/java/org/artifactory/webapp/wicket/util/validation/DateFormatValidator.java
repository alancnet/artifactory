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
import org.apache.wicket.validation.validator.StringValidator;

import java.text.SimpleDateFormat;

/**
 * Used to validate any date format which is entered
 *
 * @author Noam Tenne
 */
public class DateFormatValidator extends StringValidator {

    @Override
    protected void onValidate(IValidatable validatable) {
        String dateFormat = (String) validatable.getValue();
        try {
            new SimpleDateFormat(dateFormat);
        } catch (IllegalArgumentException e) {
            ValidationError validateError = new ValidationError();
            validateError.setMessage("Please enter a valid date format.");
            validatable.error(validateError);
        }
    }
}
