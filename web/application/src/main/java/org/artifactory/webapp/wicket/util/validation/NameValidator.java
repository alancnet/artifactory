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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.artifactory.exception.InvalidNameException;

import java.nio.file.InvalidPathException;

/**
 * @author Yoav Landman
 */
public class NameValidator extends DefaultMessageStringValidator {

    public NameValidator() {
        this(null);
    }

    public NameValidator(String errorMessage) {
        super(errorMessage);
    }

    @Override
    protected void onValidate(IValidatable<String> validatable) {
        String name = validatable.getValue();
        try {
            new InternalNameValidator().validate(name);
        } catch (InvalidNameException e) {

            ValidationError validateError = new ValidationError();
            validateError.setMessage(e.getMessage());
            validatable.error(validateError);
        }
    }

    public static class InternalNameValidator {
        private static final char[] forbiddenChars = {'/', '\\', ':', '|', '?', '*', '"', '<', '>'};

        public void validate(String name) {
            if (StringUtils.isBlank(name)) {
                throw new InvalidPathException(name, "Name cannot be blank");
            }

            if (name.equals(".") || name.equals("..") || name.equals("&")) {
                throw new InvalidPathException(name, "Name cannot be empty link");
            }

            char[] nameChars = name.toCharArray();
            for (int i = 0; i < nameChars.length; i++) {
                char c = nameChars[i];
                for (char fc : forbiddenChars) {
                    if (c == fc) {
                        throw new InvalidNameException(name, "Illegal name character: '" + c + "'", i);
                    }
                }
            }
        }
    }

}