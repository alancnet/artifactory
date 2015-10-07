package org.artifactory.ui.rest.model.admin.configuration.layouts;

import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Lior Hasson
 */
public class LayoutGridModel extends BaseModel {
    private String name;
    private String artifactPathPattern;
    private LayoutActionsModel layoutActions;

    public LayoutGridModel(RepoLayout repoLayout) {
        this.name = repoLayout.getName();
        this.artifactPathPattern = repoLayout.getArtifactPathPattern();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtifactPathPattern() {
        return artifactPathPattern;
    }

    public void setArtifactPathPattern(String artifactPathPattern) {
        this.artifactPathPattern = artifactPathPattern;
    }

    public LayoutActionsModel getLayoutActions() {
        return layoutActions;
    }

    public void setLayoutActions(LayoutActionsModel layoutActions) {
        this.layoutActions = layoutActions;
    }
}
