package org.artifactory.ui.rest.model.artifacts.search;

import java.util.ArrayList;
import java.util.List;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class BaseSearch extends BaseModel {

    private List<String> selectedRepositories = new ArrayList<>();

    public void setSelectedRepositories(List<String> selectedRepositories) {
        this.selectedRepositories = selectedRepositories;
    }

    public List<String> getSelectedRepositories() {
        return selectedRepositories;
    }
}
