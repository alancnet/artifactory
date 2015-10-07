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

package org.artifactory.webapp.wicket.page.config.license;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.VerificationResult;
import org.artifactory.api.context.ContextHelper;

import static org.artifactory.addon.license.VerificationResult.*;

/**
 * Artifactory license key Wicket validator.
 *
 * @author Noam Y. Tenne
 */
public class LicenseKeyValidator extends StringValidator {

    @Override
    protected void onValidate(IValidatable validatable) {
        String licenseKey = (String) validatable.getValue();
        licenseKey = licenseKey.trim();
        try {
            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            VerificationResult result = addonsManager.isLicenseKeyValid(licenseKey);
            if (result == error || result == invalidKey || result == converting) {
                postError(validatable, result.showMassage());
            }
        } catch (Exception e) {
            postError(validatable, e.getMessage());
        }
    }

    /**
     * Posts a validation error with the given message
     *
     * @param validatable  Validatable object
     * @param errorMessage Message to display
     */
    private void postError(IValidatable validatable, String errorMessage) {
        ValidationError error = new ValidationError();
        error.setMessage(errorMessage);
        validatable.error(error);
    }
}
