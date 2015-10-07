package org.artifactory.ui.rest.service.admin.security.ldap.groups;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ldapgroup.LdapUserGroupAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.ldap.LdapGroupModel;
import org.artifactory.ui.rest.model.admin.security.ldap.LdapImportModel;
import org.artifactory.ui.rest.model.admin.security.ldap.LdapUserGroupModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ImportLdapGroupService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ImportLdapGroupService.class);

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        LdapImportModel ldapImportModel = (LdapImportModel) request.getImodel();
        // import ldap groups to artifactory
        importGroupsToArtifactory(ldapImportModel, response);
    }

    /**
     * import ldap groups to artifactory
     * @param ldapImportModel -  ldap import model include group setting and groups to import
     * @param artifactoryResponse - encapsulate data require for response
     */
    private void importGroupsToArtifactory(LdapImportModel ldapImportModel, RestResponse artifactoryResponse) {
        List<LdapUserGroupModel> importGroups = ldapImportModel.getImportGroups();
        LdapGroupModel ldapGroupModel = ldapImportModel.getLdapGroupSettings();
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        LdapUserGroupAddon ldapGroupWebAddon = addonsManager.addonByType(LdapUserGroupAddon.class);
        try {
            ldapGroupWebAddon
                    .importLdapGroupsToArtifactory(importGroups, ldapGroupModel.getStrategy());
        } catch (Exception e) {
            String message = "An error occurred during group import" + e.getMessage();
            log.error(message);
            artifactoryResponse.error(message);
            return;
        }
        artifactoryResponse.info("Groups successfully imported");
    }
}
