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
import org.artifactory.util.PathUtils;
import org.artifactory.util.UrlValidator;

/**
 * Validates a URI. We use our own validator since the UrlValidator of wicket is broken in 1.3.5
 * (http://issues.apache.org/jira/browse/WICKET-1926).
 *
 * @author Yossi Shaul
 */
public class UriValidator extends StringValidator {

    private final transient UrlValidator urlValidator;

    /**
     * Creates new URI validator.
     *
     * @param allowedSchemes List of allowed uri schemes (http, ldap, etc.). If empty all schemes are allowed.
     */
    public UriValidator(String... allowedSchemes) {
        this.urlValidator = new UrlValidator(allowedSchemes);
    }

    @Override
    protected void onValidate(IValidatable validatable) {
        String uri = (String) validatable.getValue();

        if (!PathUtils.hasText(uri)) {
            addError(validatable, "The URL cannot be empty");
            return;
        }

        try {
            urlValidator.validate(uri);
        } catch (UrlValidator.UrlValidationException e) {
            addError(validatable, e.getMessage());
        }
    }

    protected boolean allowedSchema(String scheme) {
        return urlValidator.isAllowedSchema(scheme);
    }

    private void addError(IValidatable validatable, String message) {
        ValidationError error = new ValidationError();
        error.setMessage(message);
        validatable.error(error);
    }
}