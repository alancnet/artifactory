package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.recalculateindex;

import org.artifactory.addon.yum.YumAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.Map;

/**
 * @author Chen Keinan
 */
@JsonTypeName("YUM")
public class YumIndexCalculator extends BaseIndexCalculator {

    @Override
    public void calculateIndex() throws Exception {
        YumAddon yumAddon = addonsManager.addonByType(YumAddon.class);
        Map<String, LocalRepoDescriptor> localRepositoriesMap = ContextHelper.get().getCentralConfig().getDescriptor().getLocalRepositoriesMap();
        LocalRepoDescriptor YumRepoDescriptor = localRepositoriesMap.get(getRepoKey());
        if (yumAddon != null) {
            yumAddon.requestAsyncRepositoryYumMetadataCalculation(YumRepoDescriptor);
        }
    }
}
