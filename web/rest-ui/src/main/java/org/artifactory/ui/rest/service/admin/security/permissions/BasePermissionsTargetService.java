package org.artifactory.ui.rest.service.admin.security.permissions;

import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.permissions.PermissionTargetModel;
import org.artifactory.ui.rest.model.utils.repositories.RepoKeyType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
public abstract class BasePermissionsTargetService<T> implements RestService<T> {

    /**
     * filter repo keys based on if any remote or any local has been choose
     *
     * @param permissionTarget - permission target model
     */
    protected void filteredRepoKey(PermissionTargetModel permissionTarget) {
        List<RepoKeyType> filteredRepoKeys = new ArrayList<>();
        if (permissionTarget.isAnyLocal() && permissionTarget.isAnyRemote()) {
            filteredRepoKeys.add(new RepoKeyType("ANY", "ANY"));
        } else if (permissionTarget.isAnyLocal() && !permissionTarget.isAnyRemote()) {
            List<RepoKeyType> repoKeys = permissionTarget.getRepoKeys();
            repoKeys.forEach(repoKeyType -> {
                if (repoKeyType != null && repoKeyType.getType().equals("remote")) {
                    filteredRepoKeys.add(repoKeyType);
                }
            });
            filteredRepoKeys.add(new RepoKeyType("ANY LOCAL", "ANY LOCAL"));
        } else if (!permissionTarget.isAnyLocal() && permissionTarget.isAnyRemote()) {
            List<RepoKeyType> repoKeys = permissionTarget.getRepoKeys();
            repoKeys.stream().filter(repoKeyType -> repoKeyType.getType().equals("local")).
                    forEach(key -> filteredRepoKeys.add(key));
            filteredRepoKeys.add(new RepoKeyType("ANY REMOTE", "ANY REMOTE"));
        }
        if (!filteredRepoKeys.isEmpty()) {
            permissionTarget.setRepoKeys(filteredRepoKeys);
        }
    }
}
