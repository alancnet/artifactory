package org.artifactory.ui.rest.service.utils.setMeUp;

import com.google.common.collect.Lists;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lior Hasson
 */
public class SettingsHelper {

    /**
     * Returns a list of virtual repositories that a readable by the current user
     *
     * @return Readable virtual repository definitions
     */
    public static List<RepoDescriptor> getReadableVirtualRepoDescriptors(RepositoryService repositoryService,
            AuthorizationService authorizationService) {

        List<RepoDescriptor> readableDescriptors = Lists.newLinkedList();

        List<VirtualRepoDescriptor> virtualRepoDescriptors = repositoryService.getVirtualRepoDescriptors();

        readableDescriptors.addAll(virtualRepoDescriptors.stream()
                        .filter(virtualRepo -> isVirtualRepoReadable(null, virtualRepo, authorizationService)
                                && !virtualRepo.getKey().equals("repo"))
                        .collect(Collectors.toList())
        );

        return readableDescriptors;
    }

    /**
     * Returns a list of Remote repositories that a readable by the current user
     *
     * @return Readable Remote repository definitions
     */
    public static List<RepoDescriptor> getReadableRemoteRepoDescriptors(RepositoryService repositoryService,
            AuthorizationService authorizationService) {

        List<RepoDescriptor> readableDescriptors = Lists.newLinkedList();

        List<RemoteRepoDescriptor> RemoteRepoDescriptors = repositoryService.getRemoteRepoDescriptors();

        readableDescriptors.addAll(RemoteRepoDescriptors.stream()
                        .filter(remoteRepo ->
                                isRemoteRepoReadable(remoteRepo, authorizationService))
                        .collect(Collectors.toList())
        );

        return readableDescriptors;
    }

    /**
     * Returns a list of Remote repositories that a readable by the current user
     *
     * @return Readable Remote repository definitions
     */
    public static List<RepoDescriptor> getReadableLocalRepoDescriptors(RepositoryService repositoryService,
            AuthorizationService authorizationService) {

        List<RepoDescriptor> readableDescriptors = Lists.newLinkedList();

        List<LocalRepoDescriptor> RemoteRepoDescriptors = repositoryService.getLocalRepoDescriptors();

        readableDescriptors.addAll(RemoteRepoDescriptors.stream()
                        .filter(remoteRepo ->
                                isLocalRepoReadable(remoteRepo, authorizationService))
                        .collect(Collectors.toList())
        );

        return readableDescriptors;
    }

    /**
     * Determine if the current user has read permissions on the given virtual
     *
     * @param parentVirtualRepo     Parent virtual repo if exists. Null if not
     * @param virtualRepoDescriptor Virtual repo to test
     * @return True if is readable. False if not
     */
    public static boolean isVirtualRepoReadable(VirtualRepoDescriptor parentVirtualRepo,
            VirtualRepoDescriptor virtualRepoDescriptor, AuthorizationService authorizationService) {
        List<RepoDescriptor> aggregatedRepos = virtualRepoDescriptor.getRepositories();
        for (RepoDescriptor aggregatedRepo : aggregatedRepos) {

            if (validateReadable(parentVirtualRepo, aggregatedRepo, authorizationService)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if the current user has read permissions on the given Remote
     *
     * @param remoteRepoDescriptor     Remote repo to test
     * @return True if is readable. False if not
     */
    public static boolean isRemoteRepoReadable(RemoteRepoDescriptor remoteRepoDescriptor,
            AuthorizationService authorizationService) {
        if (validateReadable(null, remoteRepoDescriptor, authorizationService)) {
            return true;
        }

        return false;
    }

    /**
     * Determine if the current user has read permissions on the given Local
     *
     * @param localRepoDescriptor   Local repo to test
     * @return True if is readable. False if not
     */
    public static boolean isLocalRepoReadable(LocalRepoDescriptor localRepoDescriptor,
            AuthorizationService authorizationService) {
        if (validateReadable(null, localRepoDescriptor, authorizationService)) {
            return true;
        }

        return false;
    }

    public static boolean validateReadable(VirtualRepoDescriptor parentVirtualRepo, RepoDescriptor aggregatedRepo,
            AuthorizationService authorizationService) {
        String key = aggregatedRepo.getKey();
        if (aggregatedRepo instanceof HttpRepoDescriptor && aggregatedRepo.getType().isMavenGroup()) {
            if (authorizationService.canRead(InternalRepoPathFactory.repoRootPath(key + "-cache"))) {
                return true;
            }
        } else if ((aggregatedRepo instanceof VirtualRepoDescriptor) &&
                aggregatedRepo.getType().isMavenGroup() &&
                ((parentVirtualRepo == null) || !aggregatedRepo.equals(parentVirtualRepo))) {
            VirtualRepoDescriptor virtualRepo = ((VirtualRepoDescriptor) aggregatedRepo);
            if (isVirtualRepoReadable(virtualRepo, virtualRepo, authorizationService)) {
                return true;
            }
        } else {
            if (authorizationService.canRead(InternalRepoPathFactory.repoRootPath(key))
                    && aggregatedRepo.getType().isMavenGroup()) {
                return true;
            }
        }
        return false;
    }
}
