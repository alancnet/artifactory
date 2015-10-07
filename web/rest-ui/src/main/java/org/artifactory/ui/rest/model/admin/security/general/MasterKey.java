package org.artifactory.ui.rest.model.admin.security.general;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class MasterKey extends BaseModel {

    private boolean hasMasterKey = false;

    public boolean isHasMasterKey() {
        return hasMasterKey;
    }

    public void setHasMasterKey(boolean hasMasterKey) {
        this.hasMasterKey = hasMasterKey;
    }
}
