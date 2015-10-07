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

package org.artifactory.maven;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.mime.MavenNaming;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.util.StringInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author freds
 */
public class AbstractMetadataCalculator {
    private static final Logger log = LoggerFactory.getLogger(AbstractMetadataCalculator.class);

    protected final BasicStatusHolder status;

    private InternalRepositoryService repositoryService;

    public AbstractMetadataCalculator() {
        status = new BasicStatusHolder();
    }

    protected InternalRepositoryService getRepositoryService() {
        if (repositoryService == null) {
            repositoryService = (InternalRepositoryService) InternalContextHelper.get().getRepositoryService();
        }
        return repositoryService;
    }

    protected void saveMetadata(RepoPath repoPath, Metadata metadata) {
        String metadataStr;
        try {
            metadataStr = MavenModelUtils.mavenMetadataToString(metadata);
            RepoPathImpl mavenMetadataRepoPath = new RepoPathImpl(repoPath, MavenNaming.MAVEN_METADATA_NAME);
            getRepositoryService().saveFileInternal(mavenMetadataRepoPath, new StringInputStream(metadataStr));
        } catch (Exception e) {
            status.error("Error while writing metadata for " + repoPath + ".", e, log);
        }
    }
}
