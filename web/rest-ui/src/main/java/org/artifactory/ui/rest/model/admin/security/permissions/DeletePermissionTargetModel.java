package org.artifactory.ui.rest.model.admin.security.permissions;

import com.google.common.collect.Lists;
import org.artifactory.rest.common.model.RestModel;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class DeletePermissionTargetModel implements RestModel {
    private List<String> permissionTargetNames = Lists.newArrayList();

    public List<String> getPermissionTargetNames() {
        return permissionTargetNames;
    }

    public void addPermissionTargetName(String permissionTargetName) {
        permissionTargetNames.add(permissionTargetName);
    }
}