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

package org.artifactory.api.search.gavc;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.fs.ItemInfo;

/**
 * Holds GAVC search result data.
 *
 * @author Noam Tenne
 */
public class GavcSearchResult extends ArtifactSearchResult {

    private ModuleInfo moduleInfo;

    public GavcSearchResult(ItemInfo itemInfo, ModuleInfo moduleInfo) {
        super(itemInfo);

        this.moduleInfo = moduleInfo;
    }

    public String getGroupId() {
        return moduleInfo.getOrganization();
    }

    public String getArtifactId() {
        return moduleInfo.getModule();
    }

    public String getVersion() {
        StringBuilder revisionBuilder = new StringBuilder(moduleInfo.getBaseRevision());
        String artifactRevisionIntegration = moduleInfo.getFileIntegrationRevision();
        if (StringUtils.isNotBlank(artifactRevisionIntegration)) {
            revisionBuilder.append("-").append(artifactRevisionIntegration);
        }
        return revisionBuilder.toString();
    }

    public String getClassifier() {
        return moduleInfo.getClassifier();
    }
}