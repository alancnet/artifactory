package org.artifactory.ui.rest.model.admin.security.group;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class BaseGroup extends BaseModel {

    private String groupName;
    private String realm;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }
}
