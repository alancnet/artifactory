package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.builds;

import org.artifactory.rest.common.model.FileModel;
import org.artifactory.rest.common.model.RestModel;

/**
 * @author chen Keinan
 */
public class BuildJsonInfo implements RestModel, FileModel {

    private String buildJson;

    public BuildJsonInfo(String buildJson){
        this.buildJson = buildJson;
    }

    @Override
    public Object getFileResource() {
        return buildJson;
    }
}
