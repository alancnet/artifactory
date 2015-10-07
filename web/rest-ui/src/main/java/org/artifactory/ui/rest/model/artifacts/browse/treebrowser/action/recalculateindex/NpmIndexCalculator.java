package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.recalculateindex;

import org.artifactory.addon.npm.NpmAddon;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Chen Keinan
 */
@JsonTypeName("Npm")
public class NpmIndexCalculator extends BaseIndexCalculator {

    @Override
    public void calculateIndex() throws Exception {
        NpmAddon npmAddon = addonsManager.addonByType(NpmAddon.class);
        if (npmAddon != null) {
            npmAddon.reindexAsync(getRepoKey());
        }
    }
}
