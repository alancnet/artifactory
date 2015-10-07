package org.artifactory.ui.rest.model.admin.services.indexer;

import com.google.common.collect.Lists;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.index.IndexerDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Chen Keinan
 */
public class Indexer implements RestModel {

    private boolean enabled;
    private String cronExp;

    private List<String> includedRepos = Lists.newArrayList();
    private List<String> excludedRepos = Lists.newArrayList();

    Indexer() {
    }

    public Indexer(IndexerDescriptor indexerDescriptor) {
        if (indexerDescriptor != null) {
            setEnabled(indexerDescriptor.isEnabled());
            setCronExp(indexerDescriptor.getCronExp());
            populateRepos(indexerDescriptor.getIncludedRepositories());
        }
    }

    private void populateRepos(SortedSet<? extends RepoBaseDescriptor> configIncludedRepos) {
        List<String> availableMavenRepos = Lists.newArrayList();
        CentralConfigService centralConfigService = ContextHelper.get().beanForType(CentralConfigService.class);
        Map<String, LocalRepoDescriptor> localRepoDescriptorMap = centralConfigService.getDescriptor().getLocalRepositoriesMap();
        addMavenRepos(availableMavenRepos, localRepoDescriptorMap);
        Map<String, RemoteRepoDescriptor> remoteRepoDescriptorMap = centralConfigService.getDescriptor().getRemoteRepositoriesMap();
        addMavenRepos(availableMavenRepos, remoteRepoDescriptorMap);
        Map<String, VirtualRepoDescriptor> virtualRepoDescriptorMap = centralConfigService.getDescriptor().getVirtualRepositoriesMap();
        addMavenRepos(availableMavenRepos, virtualRepoDescriptorMap);
        if (!CollectionUtils.isNullOrEmpty(configIncludedRepos)) {
            configIncludedRepos.forEach(includedRepo -> includedRepos.add(includedRepo.getKey()));
        }

        // add all the rest of the repos to the exclude list
        availableMavenRepos.removeAll(includedRepos);
        excludedRepos.addAll(availableMavenRepos);
    }

    private void addMavenRepos(List<String> repos, Map<String, ? extends RepoBaseDescriptor> descriptors) {
        descriptors.values()
                .stream()
                .filter(descriptor -> descriptor.getType().isMavenGroup())
                .filter(descriptor -> !descriptor.getKey().equals(VirtualRepoDescriptor.GLOBAL_VIRTUAL_REPO_KEY))
                .forEach(descriptor -> repos.add(descriptor.getKey()));
    }

    public List<String> getIncludedRepos() {
        return includedRepos;
    }

    public void setIncludedRepos(List<String> includedRepos) {
        this.includedRepos = includedRepos;
    }

    public List<String> getExcludedRepos() {
        return excludedRepos;
    }

    public void setExcludedRepos(List<String> excludedRepos) {
        this.excludedRepos = excludedRepos;
    }

    private SortedSet<RepoBaseDescriptor> populateDescriptors(List<String> repoKeys,
            Map<String, LocalRepoDescriptor> locals, Map<String, RemoteRepoDescriptor> remotes,
            Map<String, VirtualRepoDescriptor> virtuals) {
        SortedSet<RepoBaseDescriptor> descriptors = new TreeSet<>();
        repoKeys.forEach(repoKey -> {
            if (locals.get(repoKey) != null) {
                descriptors.add(locals.get(repoKey));
            } else if (remotes.get(repoKey) != null) {
                descriptors.add(remotes.get(repoKey));
            } else if (virtuals.get(repoKey) != null) {
                descriptors.add(virtuals.get(repoKey));
            }
        });
        return descriptors;
    }

    public IndexerDescriptor toDescriptor() {
        final IndexerDescriptor desc = new IndexerDescriptor();
        desc.setCronExp(cronExp);
        desc.setEnabled(enabled);
        CentralConfigService centralConfigService = ContextHelper.get().beanForType(CentralConfigService.class);
        Map<String, LocalRepoDescriptor> locals = centralConfigService.getDescriptor().getLocalRepositoriesMap();
        Map<String, RemoteRepoDescriptor> remotes = centralConfigService.getDescriptor().getRemoteRepositoriesMap();
        Map<String, VirtualRepoDescriptor> virtuals = centralConfigService.getDescriptor().getVirtualRepositoriesMap();
        desc.setIncludedRepositories(populateDescriptors(includedRepos, locals, remotes, virtuals));
        return desc;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCronExp() {
        return cronExp;
    }

    public void setCronExp(String cronExp) {
        this.cronExp = cronExp;
    }
}
