package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.build.actionable;

import org.artifactory.build.BuildRun;
import org.artifactory.common.wicket.component.modal.ModalHandler;

/**
 * @author Yoav Aharoni
 */
public class BuildDependencyActionableItem extends BuildTabActionableItem {
    private String scope;

    public BuildDependencyActionableItem(ModalHandler textContentViewer, BuildRun buildRun, String moduleId,
            String scope) {
        super(textContentViewer, buildRun, moduleId);
        this.scope = scope;
    }

    public String getScope() {
        return scope;
    }
}