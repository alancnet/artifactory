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

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;

/**
 * Created by michaelp on 6/28/15.
 */
public class VcsUrlBuilder {

    /**
     * Formats single resource download URL based on
     * VcsGitProvider url definition
     *
     * @param provider
     * @param user
     * @param repository
     * @param file
     * @param branch
     *
     * @return formatted url
     */
    public static String resourceDownloadUrl(VcsGitProvider provider, String user, String repository, String file, String branch) {
        if(!Strings.isNullOrEmpty(provider.getResourceDownloadUrl())) {
            String[] values = new String[] {user, repository, file, branch};
            return MessageFormat.format(provider.getResourceDownloadUrl(), values);
        }
        return null;
    }

    /**
     * Formats repository download URL based on
     * VcsGitProvider url definition
     *
     * @param urlTemplate
     * @param gitOrg
     * @param gitRepo
     * @param version
     * @param fileExt
     *
     * @return formatted url
     */
    public static String repositoryDownloadUrl(String urlTemplate, String gitOrg, String gitRepo, String version, String fileExt) {
        if(!Strings.isNullOrEmpty(urlTemplate)) {
            String[] values = new String[] {gitOrg, gitRepo, version, fileExt};
            return MessageFormat.format(urlTemplate, values);
        }
        return null;
    }
}
