package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission;

import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.model.RestPaging;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

/**
 * @author Chen Keinan
 */
public class EffectivePermissionsArtifactInfo extends BaseArtifactInfo implements RestModel, RestPaging {
    private String principal;
    private String type;
    private EffectivePermission permission;

    public EffectivePermissionsArtifactInfo() {
    }

    public EffectivePermissionsArtifactInfo(String name) {
        super(name);
    }

    public EffectivePermissionsArtifactInfo(String type, String principalName, EffectivePermission permission) {
        this.permission = permission;
        this.type = type;
        this.principal = principalName;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public EffectivePermission getPermission() {
        return permission;
    }

    public void setPermission(EffectivePermission permission) {
        this.permission = permission;
    }
}
