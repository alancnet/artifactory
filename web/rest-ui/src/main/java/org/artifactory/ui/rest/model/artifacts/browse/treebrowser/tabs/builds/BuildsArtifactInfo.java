package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.builds;

import java.util.List;

import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

/**
 * @author Chen Keinan
 */
public class BuildsArtifactInfo extends BaseArtifactInfo implements RestModel {

    private List<ProduceBy> producedBy;
    private List<UsedBy> usedBy;

    public BuildsArtifactInfo() {
    }

    public BuildsArtifactInfo(String name) {
        super(name);
    }
    public BuildsArtifactInfo(List<ProduceBy> produceByRows ,List<UsedBy> usedByRows) {
        this.producedBy = produceByRows;
        this.usedBy = usedByRows;
    }
    public List<ProduceBy> getProducedBy() {
            return producedBy;
        }

    public void setProducedBy(List<ProduceBy> producedBy) {
        this.producedBy = producedBy;
    }

    public List<UsedBy> getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(List<UsedBy> usedBy) {
        this.usedBy = usedBy;
    }
    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }

}
