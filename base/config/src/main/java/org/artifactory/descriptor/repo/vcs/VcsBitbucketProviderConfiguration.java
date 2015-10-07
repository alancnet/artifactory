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

/**
 * Created by michaelp on 6/28/15.
 */
public class VcsBitbucketProviderConfiguration extends AbstractVcsProviderConfiguration {

    public VcsBitbucketProviderConfiguration() {
        super("Bitbucket",
                "",
                "{0}/{1}/get/{2}.{3}",
                "https://bitbucket.org/api/1.0/repositories/{0}/{1}/raw/{3}/{2}"
        );
    }
}