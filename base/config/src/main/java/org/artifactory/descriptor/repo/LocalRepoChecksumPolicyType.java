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
 * Checksum policy of local non-cache repositories, determines which value to return when client requests checksum.
 *
 * @author Yossi Shaul
 */
@XmlEnum(String.class)
public enum LocalRepoChecksumPolicyType {
    @XmlEnumValue("client-checksums")CLIENT("client-checksums"),
    @XmlEnumValue("server-generated-checksums")SERVER("server-generated-checksums");

    String message;

    LocalRepoChecksumPolicyType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
