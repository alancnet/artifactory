package org.artifactory.ui.rest.service.admin.security.permissions;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AclService;
import org.artifactory.factory.InfoFactory;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.security.AceInfo;
import org.artifactory.security.AclInfo;
import org.artifactory.security.MutableAceInfo;
import org.artifactory.security.MutableAclInfo;
import org.artifactory.security.MutablePermissionTargetInfo;
import org.artifactory.ui.rest.model.admin.security.permissions.PermissionTargetModel;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.EffectivePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdatePermissionsTargetService<T extends PermissionTargetModel> extends BasePermissionsTargetService<T> {
    @Autowired
    AclService aclService;
    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String permissionId = request.getPathParamByKey("name");
        if (!StringUtils.isEmpty(permissionId)) {
            // update permission target
            updatePermissionTarget(request, response, permissionId);
        } else {
            response.responseCode(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * update target permission by id
     *
     * @param artifactoryRequest  - encapsulate data related to request
     * @param artifactoryResponse - encapsulate data related to response
     * @param permissionId        - permission id
     */
    private void updatePermissionTarget(ArtifactoryRestRequest<T> artifactoryRequest, RestResponse artifactoryResponse, String permissionId) {
        PermissionTargetModel permissionTarget = artifactoryRequest.getImodel();
        // check if anny remote or any local is set and filter repo keys list accordingly
        filteredRepoKey(permissionTarget);
        AclInfo aclInfo = aclService.getAcl(permissionId);
        InfoFactory infoFactory = InfoFactoryHolder.get();
        MutableAclInfo mutableAclInfo = infoFactory.copyAcl(aclInfo);
        // update permission target data
        updatePermissionTarget(permissionTarget, infoFactory, mutableAclInfo);
        // update user and groups permissions
        updateAcesPermissions(permissionTarget, infoFactory, mutableAclInfo);
        // update acl
        aclService.updateAcl(mutableAclInfo);
        artifactoryResponse.info("Successfully updated permission target '" + permissionTarget.getName() + "'");
    }


    /**
     * update user and groups permissions
     * @param permissionTarget - permission target
     * @param infoFactory - info factory
     * @param mutableAclInfo - ace info new instance
     */
    private void updateAcesPermissions(PermissionTargetModel permissionTarget, InfoFactory infoFactory, MutableAclInfo mutableAclInfo) {
        Set<AceInfo> aclInfos = new HashSet<>();
        // update group permission
        permissionTarget.getGroups().forEach(permissionModel -> {
            MutableAceInfo ace = infoFactory.createAce(permissionModel.getPrincipal(), true, permissionModel.getMask());
            // update ace permissions
            updateAcePermission(permissionModel, ace);
            aclInfos.add(ace);
        });
        // update user permission
        permissionTarget.getUsers().forEach(permissionModel -> {
            MutableAceInfo ace = infoFactory.createAce(permissionModel.getPrincipal(), false, permissionModel.getMask());
            // update ace permissions
            updateAcePermission(permissionModel, ace);
            aclInfos.add(ace);
        });
        mutableAclInfo.setAces(aclInfos);
    }

    /**
     * update permission target data from model
     *
     * @param permissionTarget - model permission target data
     * @param infoFactory      - info factory (create permission and ace objects)
     * @param mutableAclInfo   - ace info new instance
     */
    private void updatePermissionTarget(PermissionTargetModel permissionTarget, InfoFactory infoFactory, MutableAclInfo mutableAclInfo) {
        MutablePermissionTargetInfo permission = infoFactory.createPermissionTarget();
        permission.getRepoKeys().remove("ANY");
        populatePermissionData(permissionTarget, permission);
        mutableAclInfo.setPermissionTarget(permission);
    }

    /**
     * update ace permissions
     *
     * @param permissionModel - permission model data
     * @param ace             - ace new instance
     */
    private void updateAcePermission(EffectivePermission permissionModel, MutableAceInfo ace) {
        ace.setAnnotate(permissionModel.isAnnotate());
        ace.setDelete(permissionModel.isDelete());
        ace.setDeploy(permissionModel.isDeploy());
        ace.setManage(permissionModel.isManaged());
        ace.setRead(permissionModel.isRead());
    }

    /**
     * populate permission data
     *
     * @param permissionTarget - permission target new instance
     * @param permission       - permission model
     */
    private void populatePermissionData(PermissionTargetModel permissionTarget, MutablePermissionTargetInfo permission) {
        if (!StringUtils.isEmpty(permissionTarget.getExclude())) {
            permission.setExcludes(permissionTarget.getExclude());
        }
        if (permissionTarget.getExcludePattern() != null) {
            permission.setExcludesPattern(permissionTarget.getExcludePattern());
        }
        if (permissionTarget.getIncludes() != null) {
            permission.setIncludes(permissionTarget.getIncludes());
        }
        if (permissionTarget.getIncludePattern() != null) {
            permission.setIncludesPattern(permissionTarget.getIncludePattern());
        }
        if (!StringUtils.isEmpty(permissionTarget.getName())) {
            permission.setName(permissionTarget.getName());
        }
        List<String> keys = new ArrayList<>();
        if (!StringUtils.isEmpty(permissionTarget.getRepoKeys())) {
            if (!permissionTarget.getRepoKeys().isEmpty()) {
                permissionTarget.getRepoKeys().forEach(repoKeyType -> {
                    String repoKey = repoKeyType.getRepoKey();
                    if (repoKeyType.getType().equals("remote")) {
                        repoKey = repoKey + "-cache";
                    }
                    keys.add(repoKey);
                    permission.setRepoKeys(keys);
                });
            }else{
                permission.setRepoKeys(keys);
            }
        }
    }
}
