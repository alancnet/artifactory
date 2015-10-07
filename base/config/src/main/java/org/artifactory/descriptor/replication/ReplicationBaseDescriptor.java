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

package org.artifactory.descriptor.replication;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.TaskDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Noam Y. Tenne
 */
@XmlType(name = "ReplicationBaseType", propOrder = {"enabled", "cronExp", "syncDeletes", "syncProperties", "pathPrefix",
        "repoKey"}, namespace = Descriptor.NS)
public abstract class ReplicationBaseDescriptor implements TaskDescriptor {

    @XmlElement(defaultValue = "false")
    private boolean enabled;

    @XmlElement(required = false)
    private String cronExp;

    @XmlElement(defaultValue = "true")
    private boolean syncDeletes = false;

    @XmlElement(defaultValue = "true")
    private boolean syncProperties = true;

    @XmlElement(required = false)
    private String pathPrefix;

    @XmlElement(required = true)
    private String repoKey;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCronExp() {
        return cronExp;
    }

    public void setCronExp(String cronExp) {
        this.cronExp = cronExp;
    }

    public boolean isSyncDeletes() {
        return syncDeletes;
    }

    public void setSyncDeletes(boolean syncDeletes) {
        this.syncDeletes = syncDeletes;
    }

    public boolean isSyncProperties() {
        return syncProperties;
    }

    public void setSyncProperties(boolean syncProperties) {
        this.syncProperties = syncProperties;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public RepoPath getRepoPath() {
        return InternalRepoPathFactory.create(repoKey, pathPrefix);
    }

    @Override
    public boolean sameTaskDefinition(TaskDescriptor otherDescriptor) {
        if (otherDescriptor == null || !(otherDescriptor instanceof ReplicationBaseDescriptor)) {
            throw new IllegalArgumentException(
                    "Cannot compare replication descriptor " + this + " with " + otherDescriptor);
        }
        ReplicationBaseDescriptor replicationDescriptor = (ReplicationBaseDescriptor) otherDescriptor;
        return replicationDescriptor.enabled == this.enabled &&
                StringUtils.equals(replicationDescriptor.repoKey, this.repoKey) &&
                StringUtils.equals(replicationDescriptor.cronExp, this.cronExp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReplicationBaseDescriptor)) {
            return false;
        }

        ReplicationBaseDescriptor that = (ReplicationBaseDescriptor) o;

        if (enabled != that.enabled) {
            return false;
        }
        if (syncDeletes != that.syncDeletes) {
            return false;
        }
        if (syncProperties != that.syncProperties) {
            return false;
        }
        if (cronExp != null ? !cronExp.equals(that.cronExp) : that.cronExp != null) {
            return false;
        }
        if (pathPrefix != null ? !pathPrefix.equals(that.pathPrefix) : that.pathPrefix != null) {
            return false;
        }
        if (repoKey != null ? !repoKey.equals(that.repoKey) : that.repoKey != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (enabled ? 1 : 0);
        result = 31 * result + (cronExp != null ? cronExp.hashCode() : 0);
        result = 31 * result + (syncDeletes ? 1 : 0);
        result = 31 * result + (syncProperties ? 1 : 0);
        result = 31 * result + (pathPrefix != null ? pathPrefix.hashCode() : 0);
        result = 31 * result + (repoKey != null ? repoKey.hashCode() : 0);
        return result;
    }
}
