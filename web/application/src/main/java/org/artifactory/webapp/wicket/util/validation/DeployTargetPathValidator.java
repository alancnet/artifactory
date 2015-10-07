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
import org.artifactory.util.PathValidator;

import java.nio.file.InvalidPathException;

/**
 * Validates that the deploy target path is a valid one.
 *
 * @author Yossi Shaul
 */
public class DeployTargetPathValidator extends StringValidator {

    @Override
    protected void onValidate(IValidatable<String> validatable) {
        String path = validatable.getValue();
        try {
            PathValidator.validate(path);
        } catch (InvalidPathException e) {
            ValidationError validateError = new ValidationError();
            validateError.setMessage(e.getMessage());
            validatable.error(validateError);
        }
    }

}
