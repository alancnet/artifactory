package org.artifactory.repo.interceptor;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.VcsConfiguration;
import org.artifactory.descriptor.repo.vcs.VcsType;
import org.artifactory.md.Properties;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.interceptor.storage.StorageInterceptorAdapter;
import org.artifactory.repo.vcs.git.ref.GitRef;
import org.artifactory.repo.vcs.git.ref.GitRefs;
import org.artifactory.repo.vcs.git.ref.GitRefsParser;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.util.RepoPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;

/**
 * Intercepts any Git remote cached files and attach appropriate branch/tag properties to them.
 * This implementation heavily relies on the layout of which files are stored in remote repositories.
 *
 * @author Shay Yaakov
 */
public class GitStorageInterceptor extends StorageInterceptorAdapter {
    private static final Logger log = LoggerFactory.getLogger(GitStorageInterceptor.class);

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void afterCreate(VfsItem fsItem, MutableStatusHolder statusHolder) {
        if (fsItem.isFolder()) {
            return;
        }

        LocalRepoDescriptor repoDescriptor = repositoryService.localOrCachedRepoDescriptorByKey(fsItem.getRepoKey());
        if (repoDescriptor == null || !repoDescriptor.isCache()) {
            return;
        }

        RemoteRepoDescriptor remoteRepo = ((LocalCacheRepoDescriptor) repoDescriptor).getRemoteRepo();
        VcsConfiguration vcs = remoteRepo.getVcs();
        if (vcs == null) {
            log.debug("Vcs config is not enabled for '{}', skipping interception.", fsItem.getRepoKey());
            return;
        }

        if (VcsType.GIT.equals(vcs.getType())) {
            log.debug("Vcs is not git for '{}', skipping interception.", fsItem.getRepoKey());
            return;
        }

        String path = fsItem.getPath();
        boolean isBranch = path.contains("/branches/");
        boolean isTag = path.contains("/tags/");
        if (!(isBranch || isTag)) {
            return;
        }

        ResourceStreamHandle handle = null;
        try {
            RepoPath tarballRepoPath = fsItem.getRepoPath();
            RepoPath refsRepoPath = InternalRepoPathFactory.childRepoPath(RepoPathUtils.getAncestor(tarballRepoPath, 3),
                    GitRefs.REFS_FILENAME);
            handle = repositoryService.getResourceStreamHandle(refsRepoPath);
            RepoPath refFolderRepoPath = tarballRepoPath.getParent();
            if (refFolderRepoPath == null) {
                log.warn("Could not find branch name or tag version, file '{}' is not in the proper VCS layout.",
                        tarballRepoPath);
                return;
            }
            String refName = refFolderRepoPath.getName();
            setProperties(tarballRepoPath, handle.getInputStream(), refName, isBranch);
        } catch (IOException e) {
            log.error("Unable to parse gitrefs file: " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(handle);
        }
    }

    private void setProperties(RepoPath tarballRepoPath, InputStream stream, final String refName, boolean isBranch)
            throws IOException {
        GitRefs refs = GitRefsParser.parse(stream);
        GitRef foundRef = Iterables.tryFind(isBranch ? refs.branches : refs.tags, new Predicate<GitRef>() {
            @Override
            public boolean apply(GitRef input) {
                return StringUtils.equals(input.name, refName);
            }
        }).orNull();

        if (foundRef == null) {
            log.warn("Unable to match '{}' with any tag or branch.", refName);
            return;
        }

        Properties existingProperties = repositoryService.getProperties(tarballRepoPath);
        if (existingProperties == null) {
            existingProperties = new PropertiesImpl();
        }

        if (foundRef.isBranch) {
            existingProperties.put("vcs.branch", foundRef.name);
            existingProperties.put("vcs.branchCommit", foundRef.commitId);
        } else {
            existingProperties.put("vcs.tag", foundRef.name);
        }
        repositoryService.setProperties(tarballRepoPath, existingProperties);
    }
}
