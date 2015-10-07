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

import java.util.Map;

/**
 * Created by michaelp on 6/28/15.
 */
public abstract class AbstractVcsProviderConfiguration implements VcsProviderConfiguration {

    private final String prettyText;
    private final String refsPrefix;
    private final String repositoryDownloadUrl;
    private final String resourceDownloadUrl;
    private final Map<String, String> headers;

    AbstractVcsProviderConfiguration(String prettyText, String refsPrefix, String repositoryDownloadUrl) {
        this.prettyText = prettyText;
        this.refsPrefix = refsPrefix;
        this.repositoryDownloadUrl = repositoryDownloadUrl;
        this.resourceDownloadUrl = null;
        this.headers = null;
    }

    AbstractVcsProviderConfiguration(String prettyText, String refsPrefix, String repositoryDownloadUrl,
            String resourceDownloadUrl) {
        this.prettyText = prettyText;
        this.refsPrefix = refsPrefix;
        this.repositoryDownloadUrl = repositoryDownloadUrl;
        this.resourceDownloadUrl = resourceDownloadUrl;
        this.headers = null;
    }

    AbstractVcsProviderConfiguration(String prettyText, String refsPrefix, String repositoryDownloadUrl,
            String resourceDownloadUrl, Map<String, String> headers) {
        this.prettyText = prettyText;
        this.refsPrefix = refsPrefix;
        this.repositoryDownloadUrl = repositoryDownloadUrl;
        this.resourceDownloadUrl = resourceDownloadUrl;
        this.headers = headers;
    }

    @Override
    public String getPrettyText() {
        return prettyText;
    }

    @Override
    public String getRefsPrefix() {
        return refsPrefix;
    }

    @Override
    public String getRepositoryDownloadUrl() {
        return repositoryDownloadUrl;
    }

    @Override
    public String getResourceDownloadUrl() {
        return resourceDownloadUrl;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }
}
