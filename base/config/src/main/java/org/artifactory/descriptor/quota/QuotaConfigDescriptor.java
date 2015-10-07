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

package org.artifactory.descriptor.quota;

import org.artifactory.descriptor.Descriptor;

import javax.xml.bind.annotation.XmlType;

/**
 * Descriptor for the disk space quota management
 *
 * @author Shay Yaakov
 */
@XmlType(name = "QuotaConfigType", propOrder = {"enabled", "diskSpaceLimitPercentage", "diskSpaceWarningPercentage"},
        namespace = Descriptor.NS)
public class QuotaConfigDescriptor implements Descriptor {

    private boolean enabled;
    private int diskSpaceLimitPercentage;
    private int diskSpaceWarningPercentage;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getDiskSpaceLimitPercentage() {
        return diskSpaceLimitPercentage;
    }

    public void setDiskSpaceLimitPercentage(int diskSpaceLimitPercentage) {
        this.diskSpaceLimitPercentage = diskSpaceLimitPercentage;
    }

    public int getDiskSpaceWarningPercentage() {
        return diskSpaceWarningPercentage;
    }

    public void setDiskSpaceWarningPercentage(int diskSpaceWarningPercentage) {
        this.diskSpaceWarningPercentage = diskSpaceWarningPercentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QuotaConfigDescriptor that = (QuotaConfigDescriptor) o;

        if (enabled != that.enabled) {
            return false;
        }

        if (diskSpaceLimitPercentage != that.diskSpaceLimitPercentage) {
            return false;
        }

        if (diskSpaceWarningPercentage != that.diskSpaceWarningPercentage) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (enabled ? 1 : 0);
        result = 31 * result + diskSpaceLimitPercentage;
        result = 31 * result + diskSpaceWarningPercentage;
        return result;
    }
}
