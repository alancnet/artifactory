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

package org.artifactory.descriptor.repo.vcs;

import org.artifactory.descriptor.repo.vcs.VcsArtifactoryProviderConfiguration;
import org.artifactory.descriptor.repo.vcs.VcsBitbucketProviderConfiguration;
import org.artifactory.descriptor.repo.vcs.VcsCustomProviderConfiguration;
import org.artifactory.descriptor.repo.vcs.VcsGitProviderConfiguration;
import org.artifactory.descriptor.repo.vcs.VcsProviderConfiguration;
import org.artifactory.descriptor.repo.vcs.VcsStashProviderConfiguration;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.Map;

/**
 * Enum of the VCS Git repository type.
 *
 * @author Shay Yaakov
 */
@XmlEnum(String.class)
public enum VcsGitProvider {

    @XmlEnumValue("github")
    GITHUB(new VcsGitProviderConfiguration()),

    @XmlEnumValue("bitbucket")
    BITBUCKET(new VcsBitbucketProviderConfiguration()),
    @XmlEnumValue("stash")
    STASH(new VcsStashProviderConfiguration()),

    @XmlEnumValue("artifactory")
    ARTIFACTORY(new VcsArtifactoryProviderConfiguration()),

    @XmlEnumValue("custom")
    CUSTOM(new VcsCustomProviderConfiguration());

    VcsGitProvider(VcsProviderConfiguration configuration) {
        this.configuration = configuration;
    }

    VcsProviderConfiguration configuration;

    public String getPrettyText() {
        return configuration.getPrettyText();
    }

    public String getRefsPrefix() {
        return configuration.getRefsPrefix();
    }

    public String getRepositoryDownloadUrl() {
        return configuration.getRepositoryDownloadUrl();
    }

    public String getResourceDownloadUrl() {
        return configuration.getResourceDownloadUrl();
    }

    public Map<String, String> getHeaders() {
        return configuration.getHeaders();
    }
}