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

package org.artifactory.webapp.wicket.page.security.acl;

import org.artifactory.security.AceInfo;
import org.artifactory.security.MutableAceInfo;

import java.io.Serializable;

/**
 * Used as a model for the permissions table.
 *
 * @author Yossi Shaul
 */
public class AceInfoRow implements Serializable {
    private AceInfo aceInfo;
    private MutableAceInfo mutableAceInfo;

    public static AceInfoRow createMutableAceInfoRow(MutableAceInfo aceInfo) {
        return new AceInfoRow(aceInfo);
    }

    public static AceInfoRow createAceInfoRow(AceInfo aceInfo) {
        return new AceInfoRow(aceInfo);
    }

    private AceInfoRow(AceInfo aceInfo) {
        this.aceInfo = aceInfo;
        this.mutableAceInfo = null;
    }

    private AceInfoRow(MutableAceInfo aceInfo) {
        this.aceInfo = aceInfo;
        this.mutableAceInfo = aceInfo;
    }

    public AceInfo getAceInfo() {
        return aceInfo;
    }

    public MutableAceInfo getMutableAceInfo() {
        return mutableAceInfo;
    }

    public String getPrincipal() {
        return aceInfo.getPrincipal();
    }

    public void setPrincipal(String principal) {
        checkMutable();
        mutableAceInfo.setPrincipal(principal);
    }

    public boolean isGroup() {
        return aceInfo.isGroup();
    }

    public void setGroup(boolean group) {
        checkMutable();
        mutableAceInfo.setGroup(group);
    }

    public boolean isManage() {
        return aceInfo.canManage();
    }

    public void setManage(boolean manage) {
        checkMutable();
        mutableAceInfo.setManage(manage);
        if (manage) {
            setDelete(true);
        }
    }

    public boolean isDelete() {
        return aceInfo.canDelete();
    }

    public void setDelete(boolean delete) {
        checkMutable();
        mutableAceInfo.setDelete(delete);
        if (delete) {
            setDeploy(true);
        }
    }

    public boolean isDeploy() {
        return aceInfo.canDeploy();
    }

    public void setDeploy(boolean deploy) {
        checkMutable();
        mutableAceInfo.setDeploy(deploy);
        if (deploy) {
            setAnnotate(true);
        }
    }

    public boolean isAnnotate() {
        return aceInfo.canAnnotate();
    }

    public void setAnnotate(boolean annotate) {
        checkMutable();
        mutableAceInfo.setAnnotate(annotate);
        if (annotate) {
            setRead(true);
        }
    }

    public boolean isRead() {
        return aceInfo.canRead();
    }

    public void setRead(boolean read) {
        checkMutable();
        mutableAceInfo.setRead(read);
    }

    private void checkMutable() {
        if (mutableAceInfo == null) {
            throw new IllegalStateException("Trying to modify an immutable Access Control Entry: " + aceInfo);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AceInfoRow row = (AceInfoRow) o;
        return !(aceInfo != null ? !aceInfo.equals(row.aceInfo) : row.aceInfo != null);
    }

    @Override
    public int hashCode() {
        return (aceInfo != null ? aceInfo.hashCode() : 0);
    }
}
