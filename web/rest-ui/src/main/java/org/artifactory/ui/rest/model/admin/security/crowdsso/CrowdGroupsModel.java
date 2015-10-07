package org.artifactory.ui.rest.model.admin.security.crowdsso;

import com.google.common.collect.Lists;
import org.artifactory.rest.common.model.BaseModel;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class CrowdGroupsModel extends BaseModel {
    private List<CrowdGroupModel> crowdGroupModels= Lists.newArrayList();

    public List<CrowdGroupModel> getCrowdGroupModels() {
        return crowdGroupModels;
    }

}
