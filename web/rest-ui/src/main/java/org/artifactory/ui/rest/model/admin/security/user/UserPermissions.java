package org.artifactory.ui.rest.model.admin.security.user;

import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.security.AceInfo;
import org.artifactory.security.PermissionTargetInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.EffectivePermission;

import java.util.List;

/**
 * @author Chen keinan
 */
public class UserPermissions extends BaseModel {

    private String permissionName;
    private List<String> repoKeys;
    private EffectivePermission effectivePermission;
    private int numOfRepos;

    UserPermissions() {
    }

    public UserPermissions(AceInfo aceInfo, PermissionTargetInfo permissionTargetInfo, int numOfRepos) {
        this.permissionName = permissionTargetInfo.getName();
        this.repoKeys = permissionTargetInfo.getRepoKeys();
        this.effectivePermission = new EffectivePermission(aceInfo);
        this.numOfRepos = numOfRepos;
    }
    public UserPermissions(AceInfo aceInfo, PermissionTargetInfo permissionTargetInfo) {
        this.permissionName = permissionTargetInfo.getName();
        this.repoKeys = permissionTargetInfo.getRepoKeys();
        this.effectivePermission = new EffectivePermission(aceInfo);
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public List<String> getRepoKeys() {
        return repoKeys;
    }

    public void setRepoKeys(List<String> repoKeys) {
        this.repoKeys = repoKeys;
    }

    public EffectivePermission getEffectivePermission() {
        return effectivePermission;
    }

    public void setEffectivePermission(EffectivePermission effectivePermission) {
        this.effectivePermission = effectivePermission;
    }

    public int getNumOfRepos() {
        return numOfRepos;
    }

    public void setNumOfRepos(int numOfRepos) {
        this.numOfRepos = numOfRepos;
    }
}
