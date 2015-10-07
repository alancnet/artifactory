package org.artifactory.ui.rest.model.admin.configuration.layouts;

import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Lior Hasson
 */
public class LayoutConfigViewModel extends RepoLayout implements RestModel {
    private String pathToTest;

    public LayoutConfigViewModel() {}

    public LayoutConfigViewModel(RepoLayout copy) {
        super(copy);
    }

    public String getPathToTest() {
        return pathToTest;
    }

    public void setPathToTest(String pathToTest) {
        this.pathToTest = pathToTest;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
