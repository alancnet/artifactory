/*
 * Copyright 2012 JFrog Ltd. All rights reserved.
 * JFROG PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.artifactory.addon.crowd;

import java.io.Serializable;

/**
 * @author Chen  Keinan
 */
public class CrowdExtGroup implements Serializable {

    private String groupName;

    private String description;

    private boolean existsInArtifactory = false;

    private boolean importIntoArtifactory = false;

    public CrowdExtGroup(String groupName, String description) {
        this.description = description;
        this.groupName = groupName;
    }


    public CrowdExtGroup() {
    }

    public String getDescription() {
        return description;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isExistsInArtifactory() {
        return existsInArtifactory;
    }

    public void setExistsInArtifactory(boolean existsInArtifactory) {
        this.existsInArtifactory = existsInArtifactory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CrowdExtGroup group = (CrowdExtGroup) o;

        return groupName.equals(group.groupName);
    }

    public boolean isImportIntoArtifactory() {
        return importIntoArtifactory;
    }

    public void setImportIntoArtifactory(boolean importIntoArtifactory) {
        this.importIntoArtifactory = importIntoArtifactory;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        return groupName.hashCode();
    }
}
