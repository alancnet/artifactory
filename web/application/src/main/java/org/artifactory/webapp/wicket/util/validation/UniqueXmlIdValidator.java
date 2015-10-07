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
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;

/**
 * Checks if an xml id is unique in the central config descriptor.
 *
 * @author Yossi Shaul
 */
public class UniqueXmlIdValidator extends StringValidator {
    private MutableCentralConfigDescriptor centralConfig;

    public UniqueXmlIdValidator(MutableCentralConfigDescriptor centralConfig) {
        this.centralConfig = centralConfig;
    }

    @Override
    protected void onValidate(IValidatable validatable) {
        String key = (String) validatable.getValue();
        if (!centralConfig.isKeyAvailable(key)) {
            ValidationError error = new ValidationError();
            error.setMessage(String.format("The key '%s' is already used.", key));
            validatable.error(error);
        }
    }
}