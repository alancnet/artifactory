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

package org.artifactory.common.wicket.component.file.path;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * A validator for the path auto complete fields that checks that the selected paths do not go beyond the bounds defined
 * by the root
 *
 * @author Noam Y. Tenne
 */
public class PathAutoCompleteChrootValidator extends AbstractValidator {

    private static final Logger log = LoggerFactory.getLogger(PathAutoCompleteChrootValidator.class);

    private String chroot;

    /**
     * Main constructor
     *
     * @param chroot Root to define
     */
    public PathAutoCompleteChrootValidator(String chroot) {
        this.chroot = chroot;
    }

    @Override
    protected void onValidate(IValidatable validatable) {
        File selectedFile = (File) validatable.getValue();
        try {
            if (!selectedFile.getCanonicalPath().startsWith(new File(chroot).getCanonicalPath())) {
                ValidationError error = new ValidationError();
                error.setMessage("Selected directory is out of the defined root folder bounds.");
                validatable.error(error);
            }
        } catch (IOException e) {
            ValidationError error = new ValidationError();
            error.setMessage("Error occurred during folder validation, please review the logs for further details: " +
                    e.getMessage());
            log.error("Error occurred during folder auto complete validation", e);
            validatable.error(error);
        }
    }
}