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

package org.artifactory.descriptor.repo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Enum of the checksum policies for repositories.
 *
 * @author Yossi Shaul
 */
@XmlEnum(String.class)
public enum ChecksumPolicyType {
    @XmlEnumValue("generate-if-absent")GEN_IF_ABSENT("generate-if-absent"),
    @XmlEnumValue("fail")FAIL("fail"),
    @XmlEnumValue("ignore-and-generate")IGNORE_AND_GEN("ignore-and-generate"),
    @XmlEnumValue("pass-thru")PASS_THRU("pass-thru");

    String message;

    ChecksumPolicyType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}