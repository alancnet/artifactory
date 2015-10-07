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
 * @author Shay Yaakov
 */
@XmlEnum(value = String.class)
public enum RepoType {
    @XmlEnumValue("maven")Maven("maven"),
    @XmlEnumValue("gradle")Gradle("gradle"),
    @XmlEnumValue("ivy")Ivy("ivy"),
    @XmlEnumValue("sbt")SBT("sbt"),
    @XmlEnumValue("nuget")NuGet("nuget"),
    @XmlEnumValue("gems")Gems("gems"),
    @XmlEnumValue("npm")Npm("npm"),
    @XmlEnumValue("bower")Bower("bower"),
    @XmlEnumValue("debian")Debian("debian"),
    @XmlEnumValue("pypi")Pypi("pypi"),
    @XmlEnumValue("docker")Docker("docker"),
    @XmlEnumValue("vagrant")Vagrant("vagrant"),
    @XmlEnumValue("gitlfs")GitLfs("gitlfs"),
    @XmlEnumValue("yum")YUM("yum"),
    @XmlEnumValue("vcs")VCS("vcs"),
    @XmlEnumValue("p2")P2("p2"),
    @XmlEnumValue("generic")Generic("generic");

    private String type;

    RepoType(String type) {
        this.type = type;
    }


    public boolean isMavenGroup() {
        return this == Maven || this == Ivy || this == Gradle || this == P2 || this == SBT;
    }

    public static RepoType fromType(String type) {
        for (RepoType repoType : values()) {
            if (type.equals(repoType.type)) {
                return repoType;
            }
        }
        return Generic;
    }

}
