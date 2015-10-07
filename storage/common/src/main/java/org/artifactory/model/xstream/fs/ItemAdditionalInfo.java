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

package org.artifactory.model.xstream.fs;

import org.artifactory.util.PathUtils;

import java.io.Serializable;

/**
 * @author freds
 * @date Oct 12, 2008
 */
public class ItemAdditionalInfo implements Serializable {
    private String createdBy;
    private String modifiedBy;
    /**
     * The last time the (cached) resource has been updated from it's remote location.
     */
    private long lastUpdated;

    public ItemAdditionalInfo() {
        this.lastUpdated = System.currentTimeMillis();
    }

    public ItemAdditionalInfo(ItemAdditionalInfo extension) {
        this.createdBy = extension.createdBy;
        this.modifiedBy = extension.modifiedBy;
        this.lastUpdated = extension.lastUpdated;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "ItemAdditionalInfo{" +
                "createdBy='" + createdBy + '\'' +
                ", modifiedBy='" + modifiedBy + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }

    public boolean isIdentical(ItemAdditionalInfo additionalInfo) {
        return this.lastUpdated == additionalInfo.lastUpdated &&
                PathUtils.safeStringEquals(this.modifiedBy, additionalInfo.modifiedBy) &&
                PathUtils.safeStringEquals(this.createdBy, additionalInfo.createdBy);
    }

    public boolean merge(ItemAdditionalInfo additionalInfo) {
        if (this == additionalInfo || this.isIdentical(additionalInfo)) {
            // already the same
            return false;
        }
        boolean modified = false;
        if (additionalInfo.lastUpdated > 0) {
            this.lastUpdated = additionalInfo.lastUpdated;
            modified = true;
        }
        if (PathUtils.hasText(additionalInfo.modifiedBy)) {
            this.modifiedBy = additionalInfo.modifiedBy;
            modified = true;
        }
        if (PathUtils.hasText(additionalInfo.createdBy)) {
            this.createdBy = additionalInfo.createdBy;
            modified = true;
        }
        return modified;
    }
}
