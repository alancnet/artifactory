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

package org.artifactory.descriptor.backup;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.TaskDescriptor;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@XmlType(name = "BackupType", propOrder = {"key", "enabled", "dir", "cronExp", "retentionPeriodHours", "createArchive",
        "excludedRepositories", "sendMailOnError", "excludeBuilds", "excludeNewRepositories"},
        namespace = Descriptor.NS)
public class BackupDescriptor implements TaskDescriptor {

    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_RETENTION_PERIOD_HOURS = 168;//7 days

    @XmlID
    @XmlElement(required = true)
    private String key;

    @XmlElement(defaultValue = "true")
    private boolean enabled = true;

    @XmlElement(required = true)
    private String cronExp;

    private File dir;

    @XmlElement(defaultValue = DEFAULT_RETENTION_PERIOD_HOURS + "")
    private int retentionPeriodHours = DEFAULT_RETENTION_PERIOD_HOURS;

    @XmlElement(defaultValue = "false")
    private boolean createArchive;

    @XmlIDREF
    @XmlElementWrapper(name = "excludedRepositories")
    @XmlElement(name = "repositoryRef", type = RealRepoDescriptor.class, required = false)
    private List<RealRepoDescriptor> excludedRepositories = new ArrayList<>();

    @XmlElement(defaultValue = "true")
    private boolean sendMailOnError = true;

    @XmlElement(defaultValue = "false")
    private boolean excludeBuilds = false;

    @XmlElement(defaultValue = "false")
    private boolean excludeNewRepositories = false;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public String getCronExp() {
        return cronExp;
    }

    public void setCronExp(String cronExp) {
        this.cronExp = cronExp;
    }

    public int getRetentionPeriodHours() {
        return retentionPeriodHours;
    }

    public void setRetentionPeriodHours(int retentionPeriodHours) {
        this.retentionPeriodHours = retentionPeriodHours;
    }

    public boolean isCreateArchive() {
        return createArchive;
    }

    public void setCreateArchive(boolean createArchive) {
        this.createArchive = createArchive;
    }

    @SuppressWarnings({"unchecked"})
    public List<RealRepoDescriptor> getExcludedRepositories() {
        /**
         * Even though there should not be any virtual repos in the list, it is checked again as a safety net
         * ATTENTION: Don't use generics list it will generate a ClassCastException
         */
        List erased = new ArrayList(excludedRepositories);
        for (int i = 0; i < erased.size(); i++) {
            Object anErased = erased.get(i);
            RepoDescriptor descriptorToCheck = (RepoDescriptor) anErased;
            if (!descriptorToCheck.isReal()) {
                String virtualRepoKey = descriptorToCheck.getKey();
                List<RepoDescriptor> repositories = ((VirtualRepoDescriptor) descriptorToCheck).getRepositories();
                for (RepoDescriptor descriptor : repositories) {
                    if ((descriptor.isReal()) || (virtualRepoKey.contains(descriptor.getKey()))) {
                        excludedRepositories.set(i, (RealRepoDescriptor) descriptor);
                    }
                }
            }
        }
        return excludedRepositories;
    }

    public void setExcludedRepositories(List<RealRepoDescriptor> excludedRepositories) {
        this.excludedRepositories = excludedRepositories;
    }

    public boolean removeExcludedRepository(RealRepoDescriptor realRepo) {
        return excludedRepositories.remove(realRepo);
    }

    public boolean addExcludedRepository(RealRepoDescriptor realRepo) {
        return excludeNewRepositories && excludedRepositories.add(realRepo);
    }

    public boolean isIncremental() {
        return (retentionPeriodHours <= 0) && !isCreateArchive();
    }

    public boolean isSendMailOnError() {
        return sendMailOnError;
    }

    public void setSendMailOnError(boolean sendMailOnError) {
        this.sendMailOnError = sendMailOnError;
    }

    public boolean isExcludeBuilds() {
        return excludeBuilds;
    }

    public void setExcludeBuilds(boolean excludeBuilds) {
        this.excludeBuilds = excludeBuilds;
    }

    public boolean isExcludeNewRepositories() {
        return excludeNewRepositories;
    }

    public void setExcludeNewRepositories(boolean excludeNewRepositories) {
        this.excludeNewRepositories = excludeNewRepositories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BackupDescriptor)) {
            return false;
        }

        BackupDescriptor that = (BackupDescriptor) o;

        if (key != null ? !key.equals(that.key) : that.key != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public boolean sameTaskDefinition(TaskDescriptor otherDescriptor) {
        if (otherDescriptor == null || !(otherDescriptor instanceof BackupDescriptor)) {
            throw new IllegalArgumentException("Cannot compare backup descriptor " + this + " with " + otherDescriptor);
        }
        BackupDescriptor backupDesc = (BackupDescriptor) otherDescriptor;
        return backupDesc.enabled == this.enabled &&
                StringUtils.equals(backupDesc.key, this.key) &&
                StringUtils.equals(backupDesc.cronExp, this.cronExp);
    }
}