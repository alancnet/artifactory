package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.permission;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.RestPaging;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.EffectivePermission;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.EffectivePermissionsArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.PagingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component("getEffectivePermission")
public class GetEffectivePermissionService implements RestService {

    @Autowired
    private AuthorizationService authService;

    @Autowired
    private UserGroupService userGroupService;

    public void execute(ArtifactoryRestRequest artifactoryRequest, RestResponse artifactoryResponse) {
        String path = artifactoryRequest.getQueryParamByKey("path");
        String repoKey = artifactoryRequest.getQueryParamByKey("repoKey");
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        boolean canManage = authService.canManage(repoPath);
        if (canManage) {
            // fetch principals effective permission
            List<RestPaging> effectivePermissionsArtifactInfos = fetchEffectivePermission(
                    repoPath);
            // update response with model
            PagingModel pagingModel = new PagingModel(0, effectivePermissionsArtifactInfos);
            artifactoryResponse.iModel(pagingModel);
        }
    }

    /**
     * get principals data with effective permission
     * @param repoPath - repo path
     * @return list of artifacts effective permissions
     */
    public List<RestPaging> fetchEffectivePermission(RepoPath repoPath) {
        List<UserInfo> users = userGroupService.getAllUsers(true);
        List<GroupInfo> groups = userGroupService.getAllGroups();
        List<RestPaging> principals = new ArrayList();
        for (UserInfo user : users) {
            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            CoreAddons addons = addonsManager.addonByType(CoreAddons.class);
            if (!addons.isAolAdmin(user)) {
                addUserToPrincipalList(user, repoPath, principals);
            }
        }
        for (GroupInfo group : groups) {
            addGroupToPrincipalList(group, repoPath, principals);
        }
        return principals;
    }

    /**
     * add users effective permission data if have any
     * @param userInfo - user info
     * @param repoPath - repo path
     * @param effectivePermissions
     */
    private void addUserToPrincipalList(UserInfo userInfo, RepoPath repoPath, List<RestPaging> effectivePermissions) {
        EffectivePermission effectivePermission = new EffectivePermission();
        effectivePermission.setRead(authService.canRead(userInfo, repoPath));
        effectivePermission.setAnnotate(authService.canAnnotate(userInfo, repoPath));
        effectivePermission.setDeploy(authService.canDeploy(userInfo, repoPath));
        effectivePermission.setDelete(authService.canDelete(userInfo, repoPath));
        if (effectivePermission.isHasAtLeastOnePermission()){
            effectivePermissions.add(new EffectivePermissionsArtifactInfo("user",userInfo.getUsername(),effectivePermission));
        }
     }

    /**
     * add group effective permission data if have any
     * @param groupInfo - group info
     * @param repoPath - repo path
     * @param effectivePermissions
     */
    private void addGroupToPrincipalList(GroupInfo groupInfo, RepoPath repoPath,
            List<RestPaging> effectivePermissions) {
        EffectivePermission effectivePermission = new EffectivePermission();
        effectivePermission.setRead(authService.canRead(groupInfo, repoPath));
        effectivePermission.setAnnotate(authService.canAnnotate(groupInfo, repoPath));
        effectivePermission.setDeploy(authService.canDeploy(groupInfo, repoPath));
        effectivePermission.setDelete(authService.canDelete(groupInfo, repoPath));
        if (effectivePermission.isHasAtLeastOnePermission()){
            effectivePermissions.add(new EffectivePermissionsArtifactInfo("group",groupInfo.getGroupName(),effectivePermission));
        }
      }
}
