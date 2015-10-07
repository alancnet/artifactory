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

package org.artifactory.descriptor.security;

import org.artifactory.descriptor.Descriptor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The password policy related settings.
 *
 * @author Yossi Shaul
 */
@XmlType(name = "PasswordSettingsType", namespace = Descriptor.NS)
@JsonIgnoreProperties(value = {"encryptionEnabled","encryptionRequired"})
public class PasswordSettings implements Descriptor {

    @XmlElement(defaultValue = "supported", required = false)
    private EncryptionPolicy encryptionPolicy = EncryptionPolicy.SUPPORTED;

    public EncryptionPolicy getEncryptionPolicy() {
        return encryptionPolicy;
    }

    public void setEncryptionPolicy(EncryptionPolicy encryptionPolicy) {
        this.encryptionPolicy = encryptionPolicy;
    }

    /**
     * @return True if encryption is required.
     */
    public boolean isEncryptionRequired() {
        return EncryptionPolicy.REQUIRED.equals(encryptionPolicy);
    }

    /**
     * @return True if encryption is supported\required. False if not.
     */
    public boolean isEncryptionEnabled() {
        return (EncryptionPolicy.SUPPORTED.equals(encryptionPolicy) ||
                EncryptionPolicy.REQUIRED.equals(encryptionPolicy));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PasswordSettings that = (PasswordSettings) o;

        if (encryptionPolicy != that.encryptionPolicy) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return encryptionPolicy != null ? encryptionPolicy.hashCode() : 0;
    }
}