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

import org.artifactory.descriptor.TaskDescriptor;
import org.artifactory.descriptor.property.PropertySet;

import javax.xml.bind.annotation.XmlTransient;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A non-serialized in-memory descriptor of local cache repositories.
 */
public class LocalCacheRepoDescriptor extends LocalRepoDescriptor implements TaskDescriptor {
    public static final String PATH_SUFFIX = "-cache";

    @XmlTransient
    private RemoteRepoDescriptor remoteRepo;

    public RemoteRepoDescriptor getRemoteRepo() {
        return remoteRepo;
    }

    public void setRemoteRepo(RemoteRepoDescriptor remoteRepo) {
        this.remoteRepo = remoteRepo;
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public boolean isCache() {
        return true;
    }

    @Override
    public RepoType getType() {
        return remoteRepo.getType();
    }

    @Override
    public boolean isBlackedOut() {
        return remoteRepo.isBlackedOut();
    }

    @Override
    public String getIncludesPattern() {
        return remoteRepo.getIncludesPattern();
    }

    @Override
    public String getExcludesPattern() {
        return remoteRepo.getExcludesPattern();
    }

    @Override
    public boolean isHandleReleases() {
        return remoteRepo.isHandleReleases();
    }

    @Override
    public boolean isHandleSnapshots() {
        return remoteRepo.isHandleSnapshots();
    }

    @Override
    public int getMaxUniqueSnapshots() {
        return remoteRepo.getMaxUniqueSnapshots();
    }

    @Override
    public List<PropertySet> getPropertySets() {
        return remoteRepo.getPropertySets();
    }

    @Override
    public void setPropertySets(List<PropertySet> propertySets) {
        remoteRepo.setPropertySets(propertySets);
    }

    @Override
    public boolean isPropertySetExists(String propertySetName) {
        return remoteRepo.isPropertySetExists(propertySetName);
    }

    @Override
    public void addPropertySet(PropertySet propertySet) {
        remoteRepo.addPropertySet(propertySet);
    }

    @Override
    public void updatePropertySet(PropertySet propertySet) {
        remoteRepo.updatePropertySet(propertySet);
    }

    @Override
    public PropertySet removePropertySet(String propertySetName) {
        return remoteRepo.removePropertySet(propertySetName);
    }

    @Override
    public PropertySet getPropertySet(String propertySetName) {
        return remoteRepo.getPropertySet(propertySetName);
    }

    @Override
    public boolean isArchiveBrowsingEnabled() {
        return remoteRepo.isArchiveBrowsingEnabled();
    }

    @Override
    public void setArchiveBrowsingEnabled(boolean archiveBrowsingEnabled) {
        remoteRepo.setArchiveBrowsingEnabled(archiveBrowsingEnabled);
    }

    @Override
    public boolean isForceDockerAuthentication() {
        return remoteRepo.isForceDockerAuthentication();
    }

    @Override
    public void setForceDockerAuthentication(boolean forceDockerAuthentication) {
        remoteRepo.setForceDockerAuthentication(forceDockerAuthentication);
    }

    @Override
    public boolean isForceNugetAuthentication() {
        return remoteRepo.isForceNugetAuthentication();
    }

    @Override
    public void setForceNugetAuthentication(boolean forceNugetAuthentication) {
        remoteRepo.setForceNugetAuthentication(forceNugetAuthentication);
    }

    @Override
    public boolean identicalCache(RepoDescriptor oldDescriptor) {
        if (!(oldDescriptor instanceof LocalCacheRepoDescriptor)) {
            return false;
        }
        return remoteRepo.identicalCache(((LocalCacheRepoDescriptor) oldDescriptor).remoteRepo);
    }

    @Override
    public boolean sameTaskDefinition(TaskDescriptor otherDescriptor) {
        if (otherDescriptor == null || !(otherDescriptor instanceof LocalCacheRepoDescriptor)) {
            throw new IllegalArgumentException("Cannot compare backup dexcriptor " + this + " with " + otherDescriptor);
        }
        LocalCacheRepoDescriptor localCacheRepoDesc = (LocalCacheRepoDescriptor) otherDescriptor;
        return localCacheRepoDesc.isBlackedOut() == this.isBlackedOut() &&
                localCacheRepoDesc.getKey().equals(this.getKey()) &&
                localCacheRepoDesc.remoteRepo.getUnusedArtifactsCleanupPeriodHours() == this.remoteRepo.getUnusedArtifactsCleanupPeriodHours();
    }

    public long getRetrievalCachePeriodMillis() {
        return TimeUnit.SECONDS.toMillis(remoteRepo.getRetrievalCachePeriodSecs());
    }
}