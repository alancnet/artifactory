/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.api.security;

/**
 * Service to perform encryption/decryption of config files passwords with a master encryption key.
 *
 * @author Yossi Shaul
 */
public interface MasterEncryptionService {

    /**
     * Encrypts the configuration files passwords with the master encryption key.
     * The key is created if not already exists.
     */
    void encrypt();

    /**
     * Decrypts the configuration files passwords using the existing master encryption key.
     * The key is renamed after decryption and never used again.
     */
    void decrypt();

}
