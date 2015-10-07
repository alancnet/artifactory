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

package org.artifactory.api.rest.artifact;

import java.io.Serializable;

/**
 * @author Eli Givoni
 */
public class RestFileInfo extends RestBaseStorageInfo {

    public String downloadUri;
    public String remoteUrl;
    public String mimeType;
    public String size;
    public Checksums checksums;
    public Checksums originalChecksums;

    public static class Checksums implements Serializable {
        public String sha1;
        public String md5;

        public Checksums(String sha1, String md5) {
            this.sha1 = sha1;
            this.md5 = md5;
        }

        private Checksums() {
        }
    }
}
