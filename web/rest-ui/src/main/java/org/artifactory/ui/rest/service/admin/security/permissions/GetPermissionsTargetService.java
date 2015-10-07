package org.artifactory.ui.rest.service.admin.security.permissions;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AclService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.AclInfo;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.security.PermissionTargetInfo;
import org.artifactory.ui.rest.model.admin.security.permissions.PermissionTargetModel;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.EffectivePermission;
import org.artifactory.ui.rest.model.utils.repositories.RepoKeyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetPermissionsTargetService implements RestService {
    @Autowired
    AclService aclService;
    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String id = request.getPathParamByKey("name");
        List<RepoKeyType> repoList = getAllRepositoriesData();
        if (StringUtils.isEmpty(id)) {
            fetchAllTargetPermission(response, repoList);
        }
        else {
            fetchSingleTargetPermission(response, id, repoList);
        }
    }

    /**
     * fetch Single target permission
     * @param artifactoryResponse - encapsulate data related to response
     * @param id - permission id
     * @param repoList - repository list
     */
    private void fetchSingleTargetPermission(RestResponse artifactoryResponse, String id, List<RepoKeyType> repoList) {
        AclInfo aclInfo = aclService.getAcl(id);
        // populate permission model data
        PermissionTargetInfo permission = aclInfo.getPermissionTarget();
        PermissionTargetModel permissionTarget = new PermissionTargetModel(permission);
        // filter included repo key from available repo keys
        updateSelectedAndAvailableRepo(repoList, permission, permissionTarget);
        // get groups
        aclInfo.getAces().stream().filter(ace -> ace.isGroup()).forEach(aceInfo ->
                permissionTarget.getGroups().add(new EffectivePermission(aceInfo)));
        // get users
        aclInfo.getAces().stream().filter(ace -> !ace.isGroup()).forEach(aceInfo ->
                permissionTarget.getUsers().add(new EffectivePermission(aceInfo)));
        artifactoryResponse.iModel(permissionTarget);
    }

    /**
     * fetch All target permission
     * @param artifactoryResponse - encapsulate data related to response
     * @param repoList - repository list
     */
    private void fetchAllTargetPermission(RestResponse artifactoryResponse, List<RepoKeyType> repoList) {
        List<PermissionTargetInfo> permissionTargets = aclService.getPermissionTargets(ArtifactoryPermission.MANAGE);
        List<PermissionTargetModel> permissionTargetModels = new ArrayList<>();
        permissionTargets.forEach(permissionTargetInfo -> {
            AclInfo aclInfos = aclService.getAcl(permissionTargetInfo.getName());
            // populate permission model data
            PermissionTargetModel permissionTarget = new PermissionTargetModel(permissionTargetInfo);
            // filter included repo key from available repo keys
            updateSelectedAndAvailableRepo(repoList, permissionTargetInfo, permissionTarget);
            // get groups
            aclInfos.getAces().stream().filter(ace -> ace.isGroup()).forEach(aceInfo ->
                    permissionTarget.getGroups().add(new EffectivePermission(aceInfo)));
            // get users
            aclInfos.getAces().stream().filter(ace -> !ace.isGroup()).forEach(aceInfo ->
                    permissionTarget.getUsers().add(new EffectivePermission(aceInfo)));
            permissionTargetModels.add(permissionTarget);
        });
        artifactoryResponse.iModelList(permissionTargetModels);
    }

    /**
     * update selected and available repo
     * @param repoList - repository list
     * @param permissionTargetInfo - permission target info
     * @param permissionTarget - permission target
     */
    private void updateSelectedAndAvailableRepo(List<RepoKeyType> repoList, PermissionTargetInfo permissionTargetInfo,
                                                PermissionTargetModel permissionTarget) {
        List<RepoKeyType> repoKeys = permissionTarget.getRepoKeys();
        List<String> tempRepoKeysList = new ArrayList<>();
        // selected include remote and local repositories
        if (permissionTargetInfo.getRepoKeys().contains("ANY")){
            // update select all
            updateSelectAll(repoList, permissionTarget, repoKeys);
        }else {
            // selected include all remote repositories
            if (permissionTargetInfo.getRepoKeys().contains("ANY REMOTE")) {
                // update selected any remote
                updateSelectedAnyRemote(repoList, permissionTargetInfo, permissionTarget, repoKeys, tempRepoKeysList);
                // selected include local repositories
            } else if (permissionTargetInfo.getRepoKeys().contains("ANY LOCAL")) {
                // update selected anny local
                updateSelectedAnyLocal(repoList, permissionTarget, repoKeys, tempRepoKeysList);
            } else {
                permissionTargetInfo.getRepoKeys().forEach(key -> {
                    if (key.endsWith("-cache")) {
                        tempRepoKeysList.add(key.substring(0, key.length() - 6));
                    } else {
                        tempRepoKeysList.add(key);
                    }
                });
            }
            // update available repo keys
            updateAvailableRepoKeys(repoList, permissionTarget, tempRepoKeysList);
        }
    }

    /**
     * update permission Available repo keys list
     *
     * @param repoList         - all repo list keys
     * @param permissionTarget - target permission selected repo keys
     * @param tempRepoKeysList - temp repo keys list
     */
    private void updateAvailableRepoKeys(List<RepoKeyType> repoList, PermissionTargetModel permissionTarget,
            List<String> tempRepoKeysList) {
        repoList.stream().
                filter((RepoKeyType key) -> !tempRepoKeysList.contains(key.getRepoKey())).
                forEach(key ->
                                permissionTarget.getAvailableRepoKeys().add(
                                        new RepoKeyType(key.getType(), key.getRepoKey()))
                );
    }

    /**
     * update selected list for ANY repo keys
     *
     * @param repoList         - full repo list
     * @param permissionTarget - permission target include selected repo keys
     * @param repoKeys         - selected repo list
     */
    private void updateSelectAll(List<RepoKeyType> repoList, PermissionTargetModel permissionTarget,
            List<RepoKeyType> repoKeys) {
        repoList.forEach(key -> {
            repoKeys.add(new RepoKeyType(key.getType(), key.getRepoKey()));
        });
        permissionTarget.getRepoKeys().remove("ANY");
        permissionTarget.setAnyLocal(true);
        permissionTarget.setAnyRemote(true);
    }


    /**
     * update selected any local
     * @param repoList - all repo list
     * @param permissionTarget - permission target model
     * @param repoKeys - repo keys
     */
    private void updateSelectedAnyLocal(List<RepoKeyType> repoList,
            PermissionTargetModel permissionTarget, List<RepoKeyType> repoKeys, List<String> tempList) {
        repoKeys.forEach(key -> tempList.add(key.getRepoKey()));
        repoList.stream().forEach(key -> {
            if (key.getType().equals("local")) {
                repoKeys.add(new RepoKeyType(key.getType(), key.getRepoKey()));
                tempList.add(key.getRepoKey());
            }
        });
        permissionTarget.setAnyLocal(true);
        permissionTarget.getRepoKeys().remove("ANY LOCAL");
    }

    /**
     * update selected any remote
     * @param repoList - all repo list
     * @param permissionTargetInfo - permission target info
     * @param permissionTarget - permission target model
     * @param repoKeys - repo keys
     */
    private void updateSelectedAnyRemote(List<RepoKeyType> repoList, PermissionTargetInfo permissionTargetInfo,
            PermissionTargetModel permissionTarget, List<RepoKeyType> repoKeys, List<String> tempList) {
        repoKeys.forEach(key -> tempList.add(key.getRepoKey()));
        repoList.stream().filter(key -> key.getType().equals("remote")).forEach(key -> {
            if (key.getType().equals("remote")) {
                repoKeys.add(new RepoKeyType(key.getType(), key.getRepoKey()));
            }
            tempList.add(key.getRepoKey());
        });
        permissionTarget.setAnyRemote(true);
        permissionTarget.getRepoKeys().remove("ANY REMOTE");
    }

    /**
     * return remote and local repository data
     *
     * @return list of repositories repo keys
     */
    private List<RepoKeyType> getAllRepositoriesData() {
        List<RepoKeyType> repos = new ArrayList<>();
        Map<String, LocalRepoDescriptor> localRepoDescriptorMap = centralConfigService.getDescriptor().getLocalRepositoriesMap();
        localRepoDescriptorMap.keySet().forEach(key -> repos.add(new RepoKeyType("local", key)));
        Map<String, RemoteRepoDescriptor> remoteRepoDescriptorMap = centralConfigService.getDescriptor().getRemoteRepositoriesMap();
        remoteRepoDescriptorMap.keySet().forEach(key -> repos.add(new RepoKeyType("remote", key)));
        return repos;
    }
}
