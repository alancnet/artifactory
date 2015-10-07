/*
 * Copyright 2012 JFrog Ltd. All rights reserved.
 * JFROG PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.artifactory.api.properties;

import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.common.Lock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * @author Yossi Shaul
 */
public interface PropertiesService {

    String FILTERED_RESOURCE_PROPERTY_NAME = "filtered";

    String MAVEN_PLUGIN_PROPERTY_NAME = "artifactory.maven.mavenPlugin";

    String CONTENT_TYPE_PROPERTY_NAME = "content-type";

    /**
     * @param repoPath The item (repository/folder/file) repository path
     * @return The properties attached to this repo path. Empty properties if non exist.
     */
    @Nonnull
    Properties getProperties(RepoPath repoPath);

    /**
     * Adds (and stores) a property to the item at the repo path.
     *
     * @param repoPath    The item repo path
     * @param propertySet Property set to add - can be null
     * @param property    Property to add
     * @param values      Property values (if null, will not add the property)
     */
    @Lock
    void addProperty(RepoPath repoPath, @Nullable PropertySet propertySet, Property property, String... values);

    /**
     * Edit a property on a specific repo path.
     *
     * @param repoPath    The item repo path
     * @param propertySet The property set to edit
     * @param property    The property to add
     * @param values      Property values
     */
    @Lock
    void editProperty(RepoPath repoPath, PropertySet propertySet, Property property, String... values);


    /**
     * Recursively adds (and stores) a property to the item at the repo path.
     *
     * @param repoPath    The item repo path
     * @param propertySet Property set to add - can be null
     * @param property    Property to add
     * @param values      Property values (if null, will not add the property)
     */
    @Lock
    void addPropertyRecursively(RepoPath repoPath, @Nullable PropertySet propertySet, Property property,
            String... values);

    /**
     * Deletes the property from the item.
     *
     * @param repoPath The item repo path
     * @param property Property name to delete
     */
    @Lock
    void deleteProperty(RepoPath repoPath, String property);

    /**
     * Deletes property from the item recursively.
     *
     * @param repoPath The item repo path
     * @param property Property name to delete
     */
    @Lock
    void deletePropertyRecursively(RepoPath repoPath, String property);

    /**
     * Returns map of properties for the given repo paths
     *
     * @param repoPaths     Paths to extract properties for
     * @param mandatoryKeys Any property keys that should be mandatory for resulting properties. If provided, property
     *                      objects will be added to the map only if they contain all the given keys
     * @return Map of repo paths with their corresponding properties
     */
    Map<RepoPath, Properties> getProperties(Set<RepoPath> repoPaths, String... mandatoryKeys);

    @Lock
    void setProperties(RepoPath repoPath,Properties newProperties);
}