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

import org.artifactory.descriptor.Descriptor;
import org.artifactory.util.PathUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlType(name = "VirtualRepoType", propOrder = {"artifactoryRequestsCanRetrieveRemoteArtifacts", "repositories",
        "keyPair", "pomRepositoryReferencesCleanupPolicy", "p2"}, namespace = Descriptor.NS)
public class VirtualRepoDescriptor extends RepoBaseDescriptor {

    public static final String GLOBAL_VIRTUAL_REPO_KEY = "repo";

    @XmlIDREF
    @XmlElementWrapper(name = "repositories")
    @XmlElement(name = "repositoryRef", type = RepoBaseDescriptor.class, required = false)
    private List<RepoDescriptor> repositories = new ArrayList<>();

    @XmlElement(defaultValue = "false", required = false)
    private boolean artifactoryRequestsCanRetrieveRemoteArtifacts;

    @XmlElement(required = true)
    private String keyPair;

    @XmlElement(defaultValue = "discard_active_reference", required = true)
    private PomCleanupPolicy pomRepositoryReferencesCleanupPolicy = PomCleanupPolicy.discard_active_reference;

    @XmlElement(required = false)
    private P2Configuration p2;

    public List<RepoDescriptor> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<RepoDescriptor> repositories) {
        this.repositories = repositories;
    }

    public boolean isArtifactoryRequestsCanRetrieveRemoteArtifacts() {
        return artifactoryRequestsCanRetrieveRemoteArtifacts;
    }

    public void setArtifactoryRequestsCanRetrieveRemoteArtifacts(
            boolean artifactoryRequestsCanRetrieveRemoteArtifacts) {
        this.artifactoryRequestsCanRetrieveRemoteArtifacts = artifactoryRequestsCanRetrieveRemoteArtifacts;
    }

    public String getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(String keyPair) {
        this.keyPair = keyPair;
    }

    @Override
    public boolean isReal() {
        return false;
    }

    public boolean removeRepository(RepoDescriptor repo) {
        return repositories.remove(repo);
    }

    public void removeKeyPair() {
        keyPair = null;
    }

    public PomCleanupPolicy getPomRepositoryReferencesCleanupPolicy() {
        return pomRepositoryReferencesCleanupPolicy;
    }

    public void setPomRepositoryReferencesCleanupPolicy(PomCleanupPolicy pomRepositoryReferencesCleanupPolicy) {
        this.pomRepositoryReferencesCleanupPolicy = pomRepositoryReferencesCleanupPolicy;
    }

    public P2Configuration getP2() {
        return p2;
    }

    public void setP2(P2Configuration p2) {
        this.p2 = p2;
    }

    @Override
    public boolean identicalCache(RepoDescriptor oldDescriptor) {
        if (!super.identicalCache(oldDescriptor)) {
            return false;
        }
        if (!(oldDescriptor instanceof VirtualRepoDescriptor)) {
            return false;
        }
        VirtualRepoDescriptor old = (VirtualRepoDescriptor) oldDescriptor;
        if (this.artifactoryRequestsCanRetrieveRemoteArtifacts != old.artifactoryRequestsCanRetrieveRemoteArtifacts ||
                this.pomRepositoryReferencesCleanupPolicy != old.pomRepositoryReferencesCleanupPolicy ||
                !PathUtils.safeStringEquals(this.keyPair, old.keyPair) ||
                !this.repositories.equals(old.repositories)) {
            return false;
        }
        return true;
    }
}