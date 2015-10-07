package org.artifactory.ui.rest.model.admin.security.group;

import org.artifactory.security.UserGroupInfo;

/**
 * @author Chen Keinan
 */
public class UserGroup extends BaseGroup implements UserGroupInfo{

    private boolean isExternal;

    @Override
    public boolean isExternal() {
        return isExternal;
    }

    public void setExternal(boolean isExternal) {
        this.isExternal = isExternal;
    }
}
