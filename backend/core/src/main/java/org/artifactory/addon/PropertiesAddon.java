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

package org.artifactory.addon;

import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.fs.RepoResource;
import org.artifactory.md.Properties;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.InternalRequestContext;

import java.util.Map;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public interface PropertiesAddon extends Addon {
    /**
     * Returns properties for the given repo path.
     *
     * @param repoPath Path to extract properties for
     * @return Properties of the repo path
     * @deprecated Moved from Addons to repo service
     */
    @Deprecated
    Properties getProperties(RepoPath repoPath);

    /**
     * Returns map of properties for the given repo paths.
     *
     * @param repoPaths Paths to extract properties for
     * @return Map of repo paths with their corresponding properties
     */
    Map<RepoPath, Properties> getProperties(Set<RepoPath> repoPaths);

    /**
     * Deletes the property from the item.
     *
     * @param repoPath The item repo path
     * @param property Property name to delete
     */
    void deleteProperty(RepoPath repoPath, String property);

    /**
     * Adds (and stores) a property to the item at the repo path.
     *
     * @param repoPath    The item repo path
     * @param propertySet Property set to add - can be null
     * @param property    Property to add
     * @param values      Property values (if null, will not add the property)
     */
    void addProperty(RepoPath repoPath, PropertySet propertySet, Property property, String... values);

    /**
     * set properties
     * @param repoPath - node repo path
     * @param properties - node properties
     */
    void setProperties(RepoPath repoPath, Properties properties);

    /**
     * Assemble a custom maven-metadata.xml according to the metadata definitions and matrix params in conjunction with
     * the existing properties already on the node.
     */
    RepoResource assembleDynamicMetadata(InternalRequestContext context, RepoPath metadataRepoPath);

    /**
     * update remote properties
     *
     * @param repo - repo descriptor interface
     */
    void updateRemoteProperties(Repo repo, RepoPath repoPath);
}
