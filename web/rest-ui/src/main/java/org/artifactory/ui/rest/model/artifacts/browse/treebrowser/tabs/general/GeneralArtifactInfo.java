package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.BaseInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.relatedrepositories.RelatedRepositories;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.virtualrepositories.VirtualRepositories;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class GeneralArtifactInfo extends BaseArtifactInfo {

    private BaseInfo info;
    private VirtualRepositories virtualRepositories;
    private String repoKey;
    private String path;
    private RelatedRepositories relatedRepositories;

    GeneralArtifactInfo() {
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

    public GeneralArtifactInfo(String name) {
        super(name);
    }

    public BaseInfo getInfo() {
        return info;
    }

    public void setInfo(BaseInfo info) {
        this.info = info;
    }

    public VirtualRepositories getVirtualRepositories() {
        return virtualRepositories;
    }

    public void setVirtualRepositories(VirtualRepositories virtualRepositories) {
        this.virtualRepositories = virtualRepositories;
    }

    public RepoPath retrieveRepoPath() {
        return InternalRepoPathFactory.create(getRepoKey(), getPath());
    }

    /**
     * retrieve Repository Service
     *
     * @return instance of repository service
     */
    protected RepositoryService retrieveRepoService() {
        return ContextHelper.get().beanForType(RepositoryService.class);
    }

    /**
     * retrieve local or cached repo descriptor
     *
     * @param repoPath - repo path
     * @return local repo descriptor
     */
    protected LocalRepoDescriptor localOrCachedRepoDescriptor(RepoPath repoPath) {
        String repoKey = repoPath.getRepoKey();
        return retrieveRepoService().localOrCachedRepoDescriptorByKey(repoKey);
    }

    /**
     * retrieve local repo descriptor
     *
     * @param repoPath - repo path
     * @return local repo descriptor
     */
    protected LocalRepoDescriptor localRepoDescriptor(RepoPath repoPath) {
        String repoKey = repoPath.getRepoKey();
        return retrieveRepoService().localRepoDescriptorByKey(repoKey);
    }

    /**
     * @param repoPath - repository path
     * @return Item info for specific repo path
     */
    protected ItemInfo retrieveItemInfo(RepoPath repoPath) {
        return retrieveRepoService().getItemInfo(repoPath);
    }

    /**
     * return central config service
     *
     * @return central config service instance
     */
    protected CentralConfigService retrieveCentralConfigService() {
        return ContextHelper.get().beanForType(CentralConfigService.class);
    }

    /**
     * populate virtual repositories list
     * @param baseURL
     */
    protected void populateVirtualRepositories(String baseURL) {
        RepositoryService repositoryService = retrieveRepoService();
        LocalRepoDescriptor repoDescriptor = localOrCachedRepoDescriptor(retrieveRepoPath());
        List<VirtualRepoDescriptor> virtualRepoList = repositoryService.getVirtualReposContainingRepo(repoDescriptor);
        virtualRepositories = new VirtualRepositories(virtualRepoList, baseURL);
    }

    /**
     * retrieve local or cached repo descriptor
     *
     * @param repoPath - repo path
     * @return local repo descriptor
     */
    protected RemoteRepoDescriptor remoteRepoDescriptor(RepoPath repoPath) {
        return retrieveRepoService().remoteRepoDescriptorByKey(repoPath.getRepoKey());
    }

    /**
     * retrieve local or cached repo descriptor
     *
     * @param repoPath - repo path
     * @return local repo descriptor
     */
    protected VirtualRepoDescriptor virtualRepoDescriptor(RepoPath repoPath) {
        return retrieveRepoService().virtualRepoDescriptorByKey(repoPath.getRepoKey());
    }

    public RelatedRepositories getRelatedRepositories() {
        return relatedRepositories;
    }

    public void setRelatedRepositories(
            RelatedRepositories relatedRepositories) {
        this.relatedRepositories = relatedRepositories;
    }
}
