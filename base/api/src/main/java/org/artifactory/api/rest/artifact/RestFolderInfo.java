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

import java.util.List;

/**
 * @author Eli Givoni
 */
public class RestFolderInfo extends RestBaseStorageInfo {

    public List<DirItem> children;

    public static class DirItem {
        public String uri;
        public boolean folder;

        public DirItem(String uri, boolean isFolder) {
            this.uri = uri;
            this.folder = isFolder;
        }

        private DirItem() {
        }

        @Override
        public String toString() {
            return uri + (folder ? "/" : "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DirItem)) {
                return false;
            }

            DirItem item = (DirItem) o;

            if (folder != item.folder) {
                return false;
            }
            if (uri != null ? !uri.equals(item.uri) : item.uri != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = uri != null ? uri.hashCode() : 0;
            result = 31 * result + (folder ? 1 : 0);
            return result;
        }
    }
}
