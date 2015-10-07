package org.artifactory.ui.rest.service.admin.security.auth.currentuser;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.ui.rest.model.admin.security.general.SecurityConfig;
import org.artifactory.ui.rest.model.admin.security.user.BaseUser;
import org.artifactory.ui.rest.service.admin.security.general.GetSecurityConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetCurrentUserService implements RestService {

    @Autowired
    AddonsManager addonsManager;

    @Autowired
    AuthorizationService authService;

    @Autowired
    GetSecurityConfigService getSecurityConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        boolean proWithoutLicense=false;
        if ( ! (addonsManager instanceof OssAddonsManager) && ! addonsManager.isLicenseInstalled()){
            proWithoutLicense=true;
        }
        boolean offlineMode=true;
        CentralConfigDescriptor descriptor = ContextHelper.get().getCentralConfig().getDescriptor();
        if (ConstantValues.versionQueryEnabled.getBoolean() && !descriptor.isOfflineMode()) {
            offlineMode=false;
        }
        getSecurityConfigService.execute(request, response);
        SecurityConfig securityConfig = (SecurityConfig) response.getIModel();
        boolean canManage = authService.hasPermission(ArtifactoryPermission.MANAGE);
        BaseUser baseUser = new BaseUser(authService.currentUsername(), authService.isAdmin());
        baseUser.setCanDeploy(authService.canDeployToLocalRepository());
        baseUser.setCanManage(canManage || proWithoutLicense);
        baseUser.setPreventAnonAccessBuild(securityConfig.isAnonAccessToBuildInfosDisabled());
        baseUser.setProWithoutLicense(proWithoutLicense);
        baseUser.setAnonAccessEnabled(securityConfig.isAnonAccessEnabled());
        baseUser.setOfflineMode(offlineMode);
        response.iModel(baseUser);
    }
}
