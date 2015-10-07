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

package org.artifactory.api.rest.compliance;

import java.io.Serializable;
import java.util.List;

/**
 * @author mamo
 */
public class FileComplianceInfo implements Serializable {
    public List<NameUrlPair> licenses;
    public List<NameUrlPair> vulnerabilities;

    public FileComplianceInfo() {
    }

    public static class NameUrlPair implements Serializable {
        public String name;
        public String url;

        public NameUrlPair() {
        }

        public NameUrlPair(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public static NameUrlPair of(String name, String url) {
            return new NameUrlPair(name, url);
        }
    }
}
