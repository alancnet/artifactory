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
import org.jdom2.Verifier;

/**
 * Checks if a string is a valid xsd <a href="http://www.w3.org/TR/REC-xml-names/#NT-NCName"/>NCName</a> string.
 *
 * @author Yossi Shaul
 */
public class XsdNCNameValidator extends DefaultMessageStringValidator {

    public XsdNCNameValidator() {
        this(null);
    }

    public XsdNCNameValidator(String errorMessage) {
        super(errorMessage);
    }

    @Override
    protected void onValidate(IValidatable validatable) {
        String value = (String) validatable.getValue();

        String result = Verifier.checkXMLName(value);
        if (result != null) {
            ValidationError error = new ValidationError();
            String message = errorMessage == null ? result : String.format(errorMessage, value);
            error.setMessage(message);
            validatable.error(error);
        }
    }
}