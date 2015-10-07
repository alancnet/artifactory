package org.artifactory.ui.rest.service.admin.configuration.layouts.validation;

import org.apache.commons.lang.StringUtils;
import org.artifactory.ui.rest.service.utils.validation.ValidationException;
import org.artifactory.util.RepoLayoutUtils;

/**
 *
 *
 * @author Lior Hasson
 */
public class ReservedLayoutNameValidator {
    public static void onValidate(String validatable) throws ValidationException {
        if (StringUtils.isNotBlank(validatable)) {
            if (RepoLayoutUtils.isReservedName(validatable)) {
                throw new ValidationException("The layout name '" + validatable + "' is reserved.");
            }
        }
    }
}
