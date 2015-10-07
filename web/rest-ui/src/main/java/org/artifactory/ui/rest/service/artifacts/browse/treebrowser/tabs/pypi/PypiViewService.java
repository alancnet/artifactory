package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.pypi;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.pypi.PypiAddon;
import org.artifactory.addon.pypi.PypiPkgMetadata;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.pypi.PypiArtifactInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PypiViewService implements RestService {

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        PypiArtifactInfo pypiArtifactInfo = (PypiArtifactInfo) request.getImodel();
        String path = pypiArtifactInfo.getPath();
        String repoKey = pypiArtifactInfo.getRepoKey();
        // get pypi meta data model
        PypiArtifactInfo pypiMetaData = getPypiArtifactInfo(path, repoKey);
        response.iModel(pypiMetaData);
    }

    /**
     * get pypi meta data from pypi file meta data
     *
     * @param path    - artifact path
     * @param repoKey - repo path
     * @return pypi meta data model
     */
    private PypiArtifactInfo getPypiArtifactInfo(String path, String repoKey) {
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        PypiAddon pypiAddon = addonsManager.addonByType(PypiAddon.class);
        PypiPkgMetadata pypiPkgMetadata = pypiAddon.getPypiMetadata(repoPath);
        if (pypiPkgMetadata != null) {
            return new PypiArtifactInfo(pypiPkgMetadata);
        }
        return null;
    }
}
