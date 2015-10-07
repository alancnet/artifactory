package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.recalculateindex;

import org.artifactory.addon.pypi.PypiAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.Map;

/**
 * @author Chen Keinan
 */
@JsonTypeName("Pypi")
public class PypiIndexCalculator extends BaseIndexCalculator {

    @Override
    public void calculateIndex() throws Exception {
        PypiAddon pypiAddon = addonsManager.addonByType(PypiAddon.class);
        Map<String, LocalRepoDescriptor> localRepositoriesMap = ContextHelper.get().getCentralConfig().getDescriptor().getLocalRepositoriesMap();
        LocalRepoDescriptor PypiRepoDescriptor = localRepositoriesMap.get(getRepoKey());
        if (pypiAddon != null) {
            pypiAddon.reindex(PypiRepoDescriptor, true);
        }
    }
}
