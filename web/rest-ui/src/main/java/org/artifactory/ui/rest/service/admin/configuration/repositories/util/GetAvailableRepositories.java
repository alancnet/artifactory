package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.DockerApiVersion;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.info.AvailableRepositories;
import org.artifactory.ui.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Retrieves the available repositories in the Virtual repository creation process
 *
 * @author Aviad Shikloshi
 */
@Component("allAvailableRepositories")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAvailableRepositories implements RestService {

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String type = request.getQueryParamByKey("type");
        if (StringUtils.isBlank(type)) {
            response.responseCode(HttpStatus.SC_BAD_REQUEST).error("Request must specify package type.");
            return;
        }
        Predicate<RepoDescriptor> filter = getFilter(request, type);
        List<String> localRepos = availableRepos(repositoryService.getLocalRepoDescriptors(), filter);
        List<String> remoteRepos = availableRepos(repositoryService.getRemoteRepoDescriptors(), filter);
        String currentRepoKey = RequestUtils.getRepoKeyFromRequest(request);
        List<String> virtualRepos = availableVirtualRepos(currentRepoKey, filter);
        AvailableRepositories allRepos = new AvailableRepositories(localRepos, remoteRepos, virtualRepos);
        response.iModel(allRepos);
    }

    protected Predicate<RepoDescriptor> getFilter(ArtifactoryRestRequest request, String type) {
        return repo -> filterByType(RepoType.valueOf(type), repo);
    }

    private boolean filterByType(RepoType type, RepoDescriptor repo) {
        boolean isGeneric = type.equals(RepoType.Generic);
        if (isGeneric) {
            return true;
        }

        if (type.isMavenGroup()) {
            return repo.getType().isMavenGroup();
        }

        boolean isDocker = type.equals(RepoType.Docker) && repo.getType().equals(RepoType.Docker);
        if (isDocker) {
            boolean isLocal = repo instanceof LocalRepoDescriptor;
            if (isLocal) {
                return DockerApiVersion.V2.equals(repo.getDockerApiVersion());
            }
        }

        return repo.getType().equals(type);
    }

    /**
     * List all available repositories that has the same package type as the new repository
     *
     * @param descriptors list of existing repository descriptors
     * @param filter      The filter predicate
     * @return repositories keys
     */
    private List<String> availableRepos(List<? extends RepoDescriptor> descriptors, Predicate<RepoDescriptor> filter) {
        return descriptors.stream()
                .filter(filter)
                .map(RepoDescriptor::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Collect all available virtual repositories keys, excluding the current repo key (in edit mode)
     *
     * @param repoKey the requesting current repo key
     * @param filter  The filter predicate
     * @return list of available keys
     */
    private List<String> availableVirtualRepos(String repoKey, Predicate<RepoDescriptor> filter) {
        return repositoryService.getVirtualRepoDescriptors().stream()
                .filter(filter)
                .filter(virtualDescriptor -> !virtualDescriptor.getKey().equals(VirtualRepoDescriptor.GLOBAL_VIRTUAL_REPO_KEY))
                .map(VirtualRepoDescriptor::getKey)
                .filter(currentKey -> !StringUtils.equals(repoKey, currentKey)) //exclude current to avoid cycles
                .collect(Collectors.toList());
    }
}