package org.artifactory.ui.rest.service.admin.configuration.ha;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.ui.rest.model.admin.configuration.ha.HaModel;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetHighAvailabilityMembersService implements RestService {

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("GetHighAvailabilityMembers");
            List<HaModel> artifactoryServers = getArtifactoryServers();
                response.iModelList(artifactoryServers);
    }

    /**
     * return artifactory server list if has HA license and configure
     * @return - list of artifactory servers
     */
    private List<HaModel> getArtifactoryServers() {
        List<HaModel> haModels = new ArrayList<>();
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        HaCommonAddon haCommonAddon = addonsManager.addonByType(HaCommonAddon.class);
        List<ArtifactoryServer> allArtifactoryServers = haCommonAddon.getAllArtifactoryServers();
        if (allArtifactoryServers != null && !allArtifactoryServers.isEmpty()) {
            allArtifactoryServers.forEach(server -> {
                boolean hasHeartbeat = haCommonAddon.artifactoryServerHasHeartbeat(server);
                haModels.add(new HaModel(server, !hasHeartbeat));
            });
        }
        return haModels;
    }
}
