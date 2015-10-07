package org.artifactory.ui.rest.model.admin.configuration.layouts;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Lior Hasson
 */
public class LayoutActionsModel extends BaseModel{
    private boolean copy;
    private boolean edit;
    private boolean delete;

    public LayoutActionsModel() {
        this.copy = true;
        this.edit = true;
        this.delete = true;
    }

    public boolean isCopy() {
        return copy;
    }

    public void setCopy(boolean copy) {
        this.copy = copy;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }
}
