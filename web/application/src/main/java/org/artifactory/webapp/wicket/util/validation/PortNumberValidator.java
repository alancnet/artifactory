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

import org.apache.wicket.validation.validator.RangeValidator;

/**
 * The port number field wicket validator. Asserts the input is no lower than 1, and no higher than 65535 (the valid
 * port range)
 *
 * @author Noam Y. Tenne
 */
public class PortNumberValidator extends RangeValidator<Integer> {

    /**
     * Default constructor
     */
    public PortNumberValidator() {
        super(1, 65535);
    }
}
