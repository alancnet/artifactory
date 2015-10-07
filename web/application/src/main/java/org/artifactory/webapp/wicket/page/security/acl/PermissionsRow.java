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

import org.artifactory.security.PermissionTargetInfo;

import java.io.Serializable;

/**
 * @author Tomer Cohen
 */
public class PermissionsRow implements Serializable {
    private PermissionTargetInfo permissionTarget;
    private boolean read;
    private boolean annotate;
    private boolean deploy;
    private boolean delete;
    private boolean manage;

    public PermissionsRow(PermissionTargetInfo permissionTarget) {
        this.permissionTarget = permissionTarget;
    }

    public PermissionTargetInfo getPermissionTarget() {
        return permissionTarget;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isAnnotate() {
        return annotate;
    }

    public void setAnnotate(boolean annotate) {
        this.annotate = annotate;
    }

    public boolean isDeploy() {
        return deploy;
    }

    public void setDeploy(boolean deploy) {
        this.deploy = deploy;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public boolean isManage() {
        return manage;
    }

    public void setManage(boolean manage) {
        this.manage = manage;
    }

    public boolean hasPermissions() {
        return isRead() || isDeploy() || isDelete() || isAnnotate() || isManage();
    }
}
