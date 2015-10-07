package org.artifactory.ui.rest.model.empty;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class EmptyModel extends BaseModel {
    private String empty;

    public String getEmpty() {
        return empty;
    }

    public void setEmpty(String empty) {
        this.empty = empty;
    }
}
