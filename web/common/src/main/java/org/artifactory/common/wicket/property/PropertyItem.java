/*
 * Copyright 2012 JFrog Ltd. All rights reserved.
 * JFROG PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.artifactory.common.wicket.property;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.repo.RepoPath;

import java.io.Serializable;

/**
 * Item object for property addition drop down list
 *
 * @author Noam Tenne
 */
public class PropertyItem implements Serializable {

    private PropertySet parentSet;
    private Property property;
    private RepoPath repoPath;

    /**
     * @param property Property
     * @param repoPath Selected repo path
     */
    public PropertyItem(Property property, RepoPath repoPath) {
        this(null, property, repoPath);
    }

    /**
     * @param parentSet Parent set of property
     * @param property  Property
     * @param repoPath  Selected repo path
     */
    public PropertyItem(PropertySet parentSet, Property property, RepoPath repoPath) {
        this.parentSet = parentSet;
        this.property = property;
        this.repoPath = repoPath;
    }

    /**
     * Returns the parent Property Set
     *
     * @return Partent Proeprty Set
     */
    public PropertySet getParentSet() {
        return parentSet;
    }

    /**
     * Returns the Property
     *
     * @return Property
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Returns the repo path
     *
     * @return RepoPath if set. Null if not.
     */
    public RepoPath getRepoPath() {
        return repoPath;
    }

    /**
     * Override to display property item in the format of - SET_NAME.PROPERTY_NAME
     *
     * @return Formatted display value
     */
    @Override
    public String toString() {
        String result;
        if (parentSet == null) {
            result = property.getName();
        } else {
            result = parentSet.getName();
            if (!StringUtils.isEmpty(property.getName())) {
                result += "." + property.getName();
            }
        }
        return result;
    }
}
