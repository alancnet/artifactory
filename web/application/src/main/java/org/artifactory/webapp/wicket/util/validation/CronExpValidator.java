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
import org.artifactory.webapp.wicket.util.CronUtils;

/**
 * @author Tal Abramson
 */
public class CronExpValidator extends StringValidator {

    /**
     * singleton instance
     */
    private static final CronExpValidator INSTANCE = new CronExpValidator();

    private CronExpValidator() {
        // singleton constructor
    }

    /**
     * @return the singleton instance of <code>CronExpValidator</code>
     */
    public static CronExpValidator getInstance() {
        return INSTANCE;
    }

    @Override
    protected void onValidate(IValidatable validatable) {
        String expression = (String) validatable.getValue();
        if (!CronUtils.isValid(expression)) {
            String invalidMessage = CronUtils.getInvalidMessage(expression);
            ValidationError error = new ValidationError();
            error.setMessage("Invalid cron expression" +
                    ((invalidMessage != null && !invalidMessage.isEmpty()) ? " [" + invalidMessage + "]" : "."));
            validatable.error(error);
            return;
        }
        if ((CronUtils.getNextExecution(expression) == null)) {
            ValidationError error = new ValidationError();
            error.setMessage("Cron expression cannot represents a time in the past.");
            validatable.error(error);
        }
    }
}
