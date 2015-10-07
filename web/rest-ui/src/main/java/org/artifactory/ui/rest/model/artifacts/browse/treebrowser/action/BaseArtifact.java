package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action;

import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Chen Keinan
 */
public class BaseArtifact extends BaseModel implements IAction {

    private String name;

    private String repoKey;

    private String path;

    private String param;

    public BaseArtifact(String name) {
        this.name = name;
    }

    public BaseArtifact() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
