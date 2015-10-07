package org.artifactory.ui.rest.model.admin.services.backups;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.model.RestSpecialFields;
import org.artifactory.rest.common.service.IgnoreSpecialFields;
import org.artifactory.rest.common.util.JsonUtil;
import org.codehaus.jackson.map.annotate.JsonFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Keinan
 */
@JsonFilter("exclude fields")
@IgnoreSpecialFields(value = {"excludeRepos", "includeRepos", "dir", "retentionPeriodHours",
        "excludedRepositories", "sendMailOnError", "excludeBuilds", "incremental"})
public class Backup extends BackupDescriptor implements RestModel, RestSpecialFields {

    private List<String> excludeRepos;
    private List<String> includeRepos;
    private boolean isEdit = false;
    private boolean incremental;

    Backup() {
    }

    public Backup(BackupDescriptor backupDescriptor, boolean isEdit) {
        if (backupDescriptor != null) {
            super.setCronExp(backupDescriptor.getCronExp());
            super.setEnabled(backupDescriptor.isEnabled());
            super.setKey(backupDescriptor.getKey());
            super.setExcludeNewRepositories(backupDescriptor.isExcludeNewRepositories());
            populateditFields(backupDescriptor, isEdit);
        }
    }

    /**
     * populate extra fields require for edit
     *
     * @param backupDescriptor - back up descriptor data
     * @param isEdit           - if true , populate fields for edit
     */
    private void populateditFields(BackupDescriptor backupDescriptor, boolean isEdit) {
        if (isEdit) {
            this.isEdit = isEdit;
            populateExcludeRepo(backupDescriptor.getExcludedRepositories());
            populateIncludeRepo();
            super.setRetentionPeriodHours(backupDescriptor.getRetentionPeriodHours());
            super.setCreateArchive(backupDescriptor.isCreateArchive());
            super.setDir(backupDescriptor.getDir());
            super.setExcludeBuilds(backupDescriptor.isExcludeBuilds());
            super.setRetentionPeriodHours(backupDescriptor.getRetentionPeriodHours());
            super.setSendMailOnError(backupDescriptor.isSendMailOnError());
            super.setCreateArchive(backupDescriptor.isCreateArchive());
        }
    }

    /**
     * populate exclude Real repo keys
     *
     * @param realRepoDescriptors - real repo descriptors
     */
    private void populateExcludeRepo(List<RealRepoDescriptor> realRepoDescriptors) {
        List<String> excludeRealRepo = new ArrayList<>();
        realRepoDescriptors.forEach(realRepo -> excludeRealRepo.add(realRepo.getKey()));
        excludeRepos = excludeRealRepo;
    }

    /**
     * populate exclude Real repo keys
     */
    private void populateIncludeRepo() {
        List<String> repos = new ArrayList<>();
        CentralConfigService centralConfigService = ContextHelper.get().beanForType(CentralConfigService.class);
        Map<String, LocalRepoDescriptor> localRepoDescriptorMap = centralConfigService.getDescriptor().getLocalRepositoriesMap();
        Map<String, RemoteRepoDescriptor> remoteRepoDescriptorMap = centralConfigService.getDescriptor().getRemoteRepositoriesMap();
        repos.addAll(localRepoDescriptorMap.keySet());
        repos.addAll(remoteRepoDescriptorMap.keySet());
        excludeRepos.forEach(repo -> {
            if (localRepoDescriptorMap.get(repo) != null || remoteRepoDescriptorMap.get(repo) != null) {
                repos.remove(repo);
            }
        });
        includeRepos = repos;
    }

    public List<String> getIncludeRepos() {
        return includeRepos;
    }

    public void setIncludeRepos(List<String> includeRepos) {
        this.includeRepos = includeRepos;
    }

    /**
     * populate local and remote repositories to map
     *
     * @param repoKeys                - remote repo key
     * @param realRepoDescriptors     - real repo descriptor list
     * @param localRepoDescriptorMap  - local repo descriptor map
     * @param remoteRepoDescriptorMap - remote repo descriptor map
     */
    private void populateRealRepoToMap(List<String> repoKeys, List<RealRepoDescriptor> realRepoDescriptors,
            Map<String, LocalRepoDescriptor> localRepoDescriptorMap,
            Map<String, RemoteRepoDescriptor> remoteRepoDescriptorMap) {
        repoKeys.forEach(repoKey -> {
            if (localRepoDescriptorMap.get(repoKey) != null) {
                realRepoDescriptors.add(localRepoDescriptorMap.get(repoKey));
            } else {
                if (remoteRepoDescriptorMap.get(repoKey) != null) {
                    realRepoDescriptors.add(remoteRepoDescriptorMap.get(repoKey));
                }
            }
        });
    }
    public List<String> getExcludeRepos() {
        return excludeRepos;
    }

    /**
     * get list of repo key and build list of Real repo descriptors
     *
     * @param excludeRepos - list of repos key to exclude
     */
    public void setExcludeRepos(List<String> excludeRepos) {
        this.excludeRepos = excludeRepos;
        List<RealRepoDescriptor> realRepoDescriptors = new ArrayList<>();
        CentralConfigService centralConfigService = ContextHelper.get().beanForType(CentralConfigService.class);
        Map<String, LocalRepoDescriptor> localRepoDescriptorMap = centralConfigService.getDescriptor().getLocalRepositoriesMap();
        Map<String, RemoteRepoDescriptor> remoteRepoDescriptorMap = centralConfigService.getDescriptor().getRemoteRepositoriesMap();
        populateRealRepoToMap(excludeRepos, realRepoDescriptors, localRepoDescriptorMap, remoteRepoDescriptorMap);
        super.setExcludedRepositories(realRepoDescriptors);
    }
    public String toString() {
        return JsonUtil.jsonToStringIgnoreSpecialFields(this);
    }

    @Override
    public boolean ignoreSpecialFields() {
        return !isEdit;
    }
}
