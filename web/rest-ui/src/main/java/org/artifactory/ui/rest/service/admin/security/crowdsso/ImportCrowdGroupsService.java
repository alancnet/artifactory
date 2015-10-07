package org.artifactory.ui.rest.service.admin.security.crowdsso;

import com.google.common.collect.Iterables;
import org.artifactory.addon.crowd.CrowdAddon;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.factory.InfoFactory;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.MutableGroupInfo;
import org.artifactory.ui.rest.model.admin.security.crowdsso.CrowdGroupModel;
import org.artifactory.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ImportCrowdGroupsService implements RestService {

    @Autowired
    UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        List<CrowdGroupModel> groups = (List<CrowdGroupModel>) request.getModels();
        if (CollectionUtils.isNullOrEmpty(groups)) {
            response.warn("No groups were selected for import");
            return;
        }
        Iterable<CrowdGroupModel> groupsToImport = Iterables.filter(groups, CrowdGroupModel::isImportIntoArtifactory);
        if (Iterables.isEmpty(groupsToImport)) {
            response.warn("No groups were selected for import");
            return;
        }
        // import groups
        importCrowdGroups(groupsToImport);
        // update feedback
        response.info("Groups imported successfully");
    }

    /**
     * import crowd groups
     *
     * @param groupsToImport - liist of groups to import
     */
    private void importCrowdGroups(Iterable<CrowdGroupModel> groupsToImport) {
        List<GroupInfo> artifactoryExternalGroups = userGroupService.getAllExternalGroups();
        InfoFactory factory = InfoFactoryHolder.get();
        for (CrowdGroupModel group : groupsToImport) {
            MutableGroupInfo newGroup = factory.createGroup(group.getGroupName());
            if (!artifactoryExternalGroups.contains(newGroup)) {
                newGroup.setDescription(group.getDescription());
                newGroup.setRealm(CrowdAddon.REALM);
                userGroupService.createGroup(newGroup);
                group.setExistsInArtifactory(true);
            }
        }
    }
}
