package org.artifactory.ui.rest.service.admin.security.ldap.groups;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.ldap.GroupMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GroupMappingStrategyService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String strategy = request.getQueryParamByKey("strategy");
        if (strategy.length() > 0) {
            switch (strategy) {
                case "static":
                    response.iModel(getStaticGroupMappingStrategy());
                    break;
                case "dynamic":
                    response.iModel(getDynamicGroupMappingStrategy());
                    break;
                case "hierarchy":
                    response.iModel(getHierarchyGroupMappingStrategy());
                    break;
                default:
                    response.iModel(getStaticGroupMappingStrategy());
                    break;
            }
        }
    }

    /**
     * get dynamic group mapping strategy
     *
     * @return dynamic group mapping strategy
     */
    private GroupMappingStrategy getDynamicGroupMappingStrategy() {
        GroupMappingStrategy groupMappingStrategy = new GroupMappingStrategy("memberOf", "(objectClass=group)", "cn", "description");
        return groupMappingStrategy;
    }

    /**
     * get dynamic group mapping strategy
     *
     * @return dynamic group mapping strategy
     */
    private GroupMappingStrategy getStaticGroupMappingStrategy() {
        GroupMappingStrategy groupMappingStrategy = new GroupMappingStrategy("uniqueMember", "(objectClass=groupOfNames)", "cn", "description");
        return groupMappingStrategy;
    }

    /**
     * get dynamic group mapping strategy
     *
     * @return dynamic group mapping strategy
     */
    private GroupMappingStrategy getHierarchyGroupMappingStrategy() {
        GroupMappingStrategy groupMappingStrategy = new GroupMappingStrategy("ou", "(objectClass=organizationalUnit)", "cn", "");
        return groupMappingStrategy;
    }
}
