package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.recalculateindex;

import org.artifactory.addon.bower.BowerAddon;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Chen Keinan
 */
@JsonTypeName("Bower")
public class BowerIndexCalculator extends BaseIndexCalculator {

    @Override
    public void calculateIndex() throws Exception {
        BowerAddon bowerAddon = addonsManager.addonByType(BowerAddon.class);
        if (bowerAddon != null) {
            bowerAddon.requestAsyncReindexBowerPackages(getRepoKey());
        }
    }
}
