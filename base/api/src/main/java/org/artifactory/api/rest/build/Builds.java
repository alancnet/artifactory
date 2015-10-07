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

package org.artifactory.api.rest.build;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A resource that enlists builds
 *
 * @author yoavl
 */
public class Builds implements Serializable {

    @XmlElement(name = "uri")
    public String slf;
    public List<Build> builds = new ArrayList<>();


    public static class Build {
        @XmlElement
        public String uri;
        public String lastStarted;

        public Build(String uri, String lastStarted) {
            this.lastStarted = lastStarted;
            this.uri = uri;
        }

        private Build() {
        }
    }
}