package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.recalculateindex;

import org.artifactory.addon.debian.DebianAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.Map;

/**
 * @author Chen Keinan
 */
@JsonTypeName("Debian")
public class DebianIndexCalculator extends BaseIndexCalculator {

    @Override
    public void calculateIndex() throws Exception {
        DebianAddon debianAddon = addonsManager.addonByType(DebianAddon.class);
        Map<String, LocalRepoDescriptor> localRepositoriesMap = ContextHelper.get().getCentralConfig().getDescriptor().getLocalRepositoriesMap();
        LocalRepoDescriptor debianRepoDescriptor = localRepositoriesMap.get(getRepoKey());
        if (debianAddon != null) {
            debianAddon.recalculateAll(debianRepoDescriptor, null, false);
        }
    }
}
