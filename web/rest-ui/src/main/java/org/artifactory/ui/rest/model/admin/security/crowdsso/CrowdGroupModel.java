package org.artifactory.ui.rest.model.admin.security.crowdsso;

import org.artifactory.addon.crowd.CrowdExtGroup;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Chen Keinan
 */
public class CrowdGroupModel extends CrowdExtGroup implements RestModel {

    public CrowdGroupModel(String groupName, String description) {
        super(groupName, description);
    }

    public CrowdGroupModel() {
        super();
    }

    public CrowdGroupModel(CrowdExtGroup crowdExtGroup) {
        super.setDescription(crowdExtGroup.getDescription());
        super.setGroupName(crowdExtGroup.getGroupName());
        super.setExistsInArtifactory(crowdExtGroup.isExistsInArtifactory());
        super.setImportIntoArtifactory(crowdExtGroup.isExistsInArtifactory());
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
