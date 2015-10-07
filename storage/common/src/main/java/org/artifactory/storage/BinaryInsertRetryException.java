/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.storage;

import org.artifactory.binstore.BinaryInfo;

/**
 * Exception used for indicating a failure to update storage due to the binary already stored.
 *
 * @author Fred Simon
 */
public class BinaryInsertRetryException extends StorageException {
    private final BinaryInfo binaryInfo;

    public BinaryInsertRetryException(BinaryInfo binaryInfo, Throwable cause) {
        super("Insertion of " + binaryInfo + " failed. Auto retry exception", cause);
        this.binaryInfo = binaryInfo;
    }

    public BinaryInfo getBinaryInfo() {
        return binaryInfo;
    }
}
