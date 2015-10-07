package org.artifactory.ui.rest.service.utils.groups;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AclService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.AceInfo;
import org.artifactory.security.PermissionTargetInfo;
import org.artifactory.ui.rest.model.admin.security.group.Group;
import org.artifactory.ui.rest.model.admin.security.user.UserPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Keian
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetGroupPermissionsService implements RestService<Group> {

    @Autowired
    UserGroupService userGroupService;

    @Autowired
    AclService aclService;

    @Override
    public void execute(ArtifactoryRestRequest<Group> request, RestResponse response) {
        Group group = request.getImodel();
        List<UserPermissions> userPermissionsList = new ArrayList<>();
        // get group permissions
        Map<PermissionTargetInfo, AceInfo> userPermissionByPrincipal = aclService.getGroupsPermissions(group.getGroups());
        userPermissionByPrincipal.forEach((permission, ace) -> {
            int repoKeysSize = getRepoKeysSize(permission);
            userPermissionsList.add(new UserPermissions(ace, permission, repoKeysSize));
        });
        response.iModelList(userPermissionsList);
    }

    /**
     * get the number of repo keys size
     *
     * @param permission - permission target
     * @return
     */
    private int getRepoKeysSize(PermissionTargetInfo permission) {
        if (permission.getRepoKeys().contains("ANY")) {
            RepositoryService repositoryService = ContextHelper.get().getRepositoryService();
            return repositoryService.getAllRepoKeys().size();
        } else if (permission.getRepoKeys().contains("ANY LOCAL")) {
            RepositoryService repositoryService = ContextHelper.get().getRepositoryService();
            return repositoryService.getLocalRepoDescriptors().size();
        } else if (permission.getRepoKeys().contains("ANY REMOTE")) {
            RepositoryService repositoryService = ContextHelper.get().getRepositoryService();
            return repositoryService.getCachedRepoDescriptors().size();
        }
        return 0;
    }
}
