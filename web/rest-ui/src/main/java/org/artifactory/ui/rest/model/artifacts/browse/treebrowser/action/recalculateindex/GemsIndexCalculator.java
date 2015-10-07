package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.recalculateindex;

import org.artifactory.addon.gems.GemsAddon;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Chen Keinan
 */
@JsonTypeName("Gems")
public class GemsIndexCalculator extends BaseIndexCalculator {

    @Override
    public void calculateIndex() throws Exception {
        GemsAddon gemsAddon = addonsManager.addonByType(GemsAddon.class);
        if (gemsAddon != null) {
            gemsAddon.reindexAsync(getRepoKey());
        }
    }
}
