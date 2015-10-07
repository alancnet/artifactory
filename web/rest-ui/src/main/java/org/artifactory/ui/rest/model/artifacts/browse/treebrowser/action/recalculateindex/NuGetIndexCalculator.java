package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.recalculateindex;

import org.artifactory.addon.nuget.UiNuGetAddon;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Chen Keinan
 */
@JsonTypeName("NuGet")
public class NuGetIndexCalculator extends BaseIndexCalculator {

    @Override
    public void calculateIndex() throws Exception {
        UiNuGetAddon npmAddon = addonsManager.addonByType(UiNuGetAddon.class);
        if (npmAddon != null) {
            npmAddon.requestAsyncReindexNuPkgs(getRepoKey());
        }
    }
}
