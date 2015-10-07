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

package org.artifactory.repo;


import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.repo.PomCleanupPolicy;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * Virtual repository configuration
 *
 * @author Tomer Cohen
 * @see org.artifactory.descriptor.repo.VirtualRepoDescriptor
 */
public class VirtualRepositoryConfigurationImpl extends RepositoryConfigurationBase
        implements VirtualRepositoryConfiguration {

    private List<String> repositories;
    private boolean artifactoryRequestsCanRetrieveRemoteArtifacts = false;
    private String keyPair = "";
    private String pomRepositoryReferencesCleanupPolicy = "discard_active_reference";

    public VirtualRepositoryConfigurationImpl() {
    }

    public VirtualRepositoryConfigurationImpl(VirtualRepoDescriptor repoDescriptor) {
        super(repoDescriptor, TYPE);
        setArtifactoryRequestsCanRetrieveRemoteArtifacts(
                repoDescriptor.isArtifactoryRequestsCanRetrieveRemoteArtifacts());
        String keyPair = repoDescriptor.getKeyPair();
        if (StringUtils.isNotBlank(keyPair)) {
            setKeyPair(keyPair);
        }
        Map<String, String> pomCleanupPolicies = extractXmlValueFromEnumAnnotations(PomCleanupPolicy.class);
        for (Map.Entry<String, String> pomCleanupPolicy : pomCleanupPolicies.entrySet()) {
            if (pomCleanupPolicy.getKey().equals(repoDescriptor.getPomRepositoryReferencesCleanupPolicy().name())) {
                setPomRepositoryReferencesCleanupPolicy(pomCleanupPolicy.getKey());
            }
        }
        List<RepoDescriptor> repositories = repoDescriptor.getRepositories();
        setRepositories(Lists.transform(repositories, new Function<RepoDescriptor, String>() {
            @Override
            public String apply(@Nonnull RepoDescriptor input) {
                return input.getKey();
            }
        }));
    }

    @Override
    public boolean isArtifactoryRequestsCanRetrieveRemoteArtifacts() {
        return artifactoryRequestsCanRetrieveRemoteArtifacts;
    }

    public void setArtifactoryRequestsCanRetrieveRemoteArtifacts(
            boolean artifactoryRequestsCanRetrieveRemoteArtifacts) {
        this.artifactoryRequestsCanRetrieveRemoteArtifacts = artifactoryRequestsCanRetrieveRemoteArtifacts;
    }

    @Override
    public String getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(String keyPair) {
        this.keyPair = keyPair;
    }

    @Override
    public String getPomRepositoryReferencesCleanupPolicy() {
        return pomRepositoryReferencesCleanupPolicy;
    }

    public void setPomRepositoryReferencesCleanupPolicy(String pomRepositoryReferencesCleanupPolicy) {
        this.pomRepositoryReferencesCleanupPolicy = pomRepositoryReferencesCleanupPolicy;
    }

    @Override
    public List<String> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }
}
