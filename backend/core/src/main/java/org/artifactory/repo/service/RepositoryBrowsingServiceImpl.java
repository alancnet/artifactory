/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.repo.service;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.repo.BaseBrowsableItem;
import org.artifactory.api.repo.BrowsableItem;
import org.artifactory.api.repo.BrowsableItemCriteria;
import org.artifactory.api.repo.RemoteBrowsableItem;
import org.artifactory.api.repo.RepositoryBrowsingService;
import org.artifactory.api.repo.RootNodesFilterResult;
import org.artifactory.api.repo.VirtualBrowsableItem;
import org.artifactory.api.repo.VirtualRepoItem;
import org.artifactory.api.repo.exception.FolderExpectedException;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.md.Properties;
import org.artifactory.mime.MavenNaming;
import org.artifactory.repo.ArtifactoryStandardUrlResolver;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RealRepo;
import org.artifactory.repo.RemoteRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.remote.browse.RemoteItem;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.storage.fs.service.PropertiesService;
import org.artifactory.storage.fs.tree.ItemNode;
import org.artifactory.storage.fs.tree.ItemTree;
import org.artifactory.storage.fs.tree.TreeBrowsingCriteriaBuilder;
import org.artifactory.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
@Service
public class RepositoryBrowsingServiceImpl implements RepositoryBrowsingService {
    private static final Logger log = LoggerFactory.getLogger(RepositoryBrowsingServiceImpl.class);

    @Autowired
    private AuthorizationService authService;

    @Autowired
    private InternalRepositoryService repoService;

    @Autowired
    private PropertiesService propertiesService;

    @Override
    public BrowsableItem getLocalRepoBrowsableItem(RepoPath repoPath) {
        ItemInfo itemInfo = getItemInfo(repoPath);
        return (itemInfo != null) ? BrowsableItem.getItem(itemInfo) : null;
    }

    @Override
    public VirtualBrowsableItem getVirtualRepoBrowsableItem(RepoPath repoPath) {
        log.debug("getting new Virtual Repo '{}' Browsable Item", repoPath.getRepoKey());

        VirtualRepoItem virtualRepoItem = getVirtualRepoItem(repoPath);
        if (virtualRepoItem != null) {
            ItemInfo itemInfo = virtualRepoItem.getItemInfo();

            log.debug("new Virtual Repo Browsable Item ,name:'{}',created:'{}',lastModified:'{}',size:'{}', ",
                    itemInfo.getName(), itemInfo.getCreated(),
                    itemInfo.getLastModified(), itemInfo.isFolder() ? -1 : ((FileInfo) itemInfo).getSize());

            return new VirtualBrowsableItem(itemInfo.getName(), itemInfo.isFolder(), itemInfo.getCreated(),
                    itemInfo.getLastModified(), itemInfo.isFolder() ? -1 : ((FileInfo) itemInfo).getSize(),
                    repoPath, virtualRepoItem.getRepoKeys());
        } else {
            return null;
        }
    }

    private ItemInfo getItemInfo(RepoPath repoPath) {
        LocalRepo repo = repoService.localOrCachedRepositoryByKey(repoPath.getRepoKey());
        if (repo == null) {
            log.trace("No local or cache repo found:'{}'", repoPath.getRepoKey());
            throw new IllegalArgumentException("No local or cache repo found: " + repoPath.getRepoKey());
        }

        if (repo.isBlackedOut()) {
            return null;
        }

        return repoService.getItemInfo(repoPath);
    }

    @Override
    @Nonnull
    public List<BaseBrowsableItem> getLocalRepoBrowsableChildren(BrowsableItemCriteria criteria) {
        return getLocalRepoBrowsableChildrenData(criteria,false,null);
    }

    @Nullable
    @Override
    public List<BaseBrowsableItem> getLocalRepoBrowsableChildren(@Nonnull BrowsableItemCriteria criteria,
            boolean updateRootNodesFilterFlag, RootNodesFilterResult browsableItemAccept) {
        return getLocalRepoBrowsableChildrenData(criteria, updateRootNodesFilterFlag,browsableItemAccept);
    }

    private List<BaseBrowsableItem> getLocalRepoBrowsableChildrenData(BrowsableItemCriteria criteria,
            boolean updateRootNodesFilterFlag,RootNodesFilterResult browsableItemAccept) {
        RepoPath repoPath = criteria.getRepoPath();
        LocalRepo repo = repoService.localOrCachedRepositoryByKey(repoPath.getRepoKey());
        if (repo == null) {
            log.trace("No local or cache repo found:'{}'", repoPath.getRepoKey());
            throw new IllegalArgumentException("No local or cache repo found: " + repoPath.getRepoKey());
        }

        if (repo.isBlackedOut() || !repo.accepts(repoPath)) {
            return Lists.newArrayListWithCapacity(0);
        }

        ItemTree tree = new ItemTree(criteria.getRepoPath(), new TreeBrowsingCriteriaBuilder()
                .applyRepoIncludeExclude().applySecurity().cacheChildren(false).build());
        ItemNode rootNode = tree.getRootNode();
        if (rootNode == null) {
            log.trace("No local or cache repo found:'{}'", repoPath.getRepoKey());
            throw new ItemNotFoundRuntimeException(repoPath);
        }
        if (!rootNode.isFolder()) {
            log.trace("repo '{}' root node is not folder", repoPath.getRepoKey());
            throw new FolderExpectedException(repoPath);
        }
        List<ItemNode> children = getRootNodeChildren(updateRootNodesFilterFlag,browsableItemAccept,rootNode);
        if (children.isEmpty()) {
            return Lists.newArrayListWithCapacity(0);
        }

        List<BaseBrowsableItem> repoPathChildren = Lists.newArrayList();
        for (ItemNode child : children) {
            //Check if we should return the child
            ItemInfo childItemInfo = child.getItemInfo();
            BrowsableItem browsableItem = BrowsableItem.getItem(childItemInfo);
            if (child.isFolder()) {
                repoPathChildren.add(browsableItem);
            } else if (isPropertiesMatch(childItemInfo, criteria.getRequestProperties())) {   // match props for files
                repoPathChildren.add(browsableItem);
                if (criteria.isIncludeChecksums()) {
                    repoPathChildren.addAll(getBrowsableItemChecksumItems(repo,
                            ((FileInfo) childItemInfo).getChecksumsInfo(), browsableItem));
                }
            }
        }

        Collections.sort(repoPathChildren);
        return repoPathChildren;
    }

    /**
     * call get children with monitor Filter Acceptance if flag is active or base get children
     * @param updateRootNodesFilterFlag - if true keep flag from empty list due to
     *                                      no access (canRead = false) return
     * @param rootNodesFilterResult -
     * @param rootNode
     * @return children nodes List or empty list with updated browsableItemAccept canRead flag when
     * updateRootNodesFilterFlag is active
     */
    private List<ItemNode> getRootNodeChildren(boolean updateRootNodesFilterFlag,
            RootNodesFilterResult rootNodesFilterResult, ItemNode rootNode) {
        List<ItemNode> children;
        if (updateRootNodesFilterFlag) {
           children = rootNode.getChildren(updateRootNodesFilterFlag,rootNodesFilterResult);
        }
        else{
            children = rootNode.getChildren();
        }
        return children;
    }

    private boolean canRead(RealRepo repo, RepoPath childRepoPath) {
        return authService.canRead(childRepoPath) && repo.accepts(childRepoPath);
    }

    private boolean isPropertiesMatch(ItemInfo itemInfo, Properties requestProps) {
        if (requestProps == null || requestProps.isEmpty()) {
            return true;
        }
        Properties nodeProps = propertiesService.getProperties(itemInfo.getRepoPath());
        Properties.MatchResult result = nodeProps.matchQuery(requestProps);
        return !Properties.MatchResult.CONFLICT.equals(result);
    }

    @Override
    @Nonnull
    public List<BaseBrowsableItem> getRemoteRepoBrowsableChildren(BrowsableItemCriteria criteria) {
        return getRemoteRepoBrowsableChildrenData(criteria,false,null);
    }

    @Override
    @Nonnull
    public List<BaseBrowsableItem> getRemoteRepoBrowsableChildren(BrowsableItemCriteria criteria,
            boolean updateRootNodesFilterFlag, RootNodesFilterResult rootNodesFilterResult) {
        return getRemoteRepoBrowsableChildrenData(criteria, updateRootNodesFilterFlag,rootNodesFilterResult);
    }

    private List<BaseBrowsableItem> getRemoteRepoBrowsableChildrenData(BrowsableItemCriteria criteria,
            boolean updateRootNodesFilterFlag, RootNodesFilterResult rootNodesFilterResult) {
        RepoPath repoPath = criteria.getRepoPath();
        String repoKey = repoPath.getRepoKey();
        String relativePath = repoPath.getPath();
        RemoteRepo repo = repoService.remoteRepositoryByKey(repoKey);
        if (repo == null) {
            log.trace("Remote repo not found:'{}'", repoKey);
            throw new IllegalArgumentException("Remote repo not found: " + repoKey);
        }
        log.debug("Getting Remote Repo '{}' Browsable Children", repoKey);
        // include remote resources based on the flag and the offline mode
        boolean includeRemoteResources = criteria.isIncludeRemoteResources() &&
                repo.isListRemoteFolderItems() && repo.accepts(repoPath);

        // first get all the cached items
        List<BaseBrowsableItem> children = Lists.newArrayList();
        boolean pathExistsInCache = false;
        if (repo.isStoreArtifactsLocally()) {
            try {
                BrowsableItemCriteria cacheCriteria = new BrowsableItemCriteria.Builder(criteria).
                        repoPath(InternalRepoPathFactory.create(repo.getLocalCacheRepo().getKey(),
                                relativePath)).build();
                if (updateRootNodesFilterFlag){
                    children = getLocalRepoBrowsableChildren(cacheCriteria, updateRootNodesFilterFlag,rootNodesFilterResult);
                }else {
                    children = getLocalRepoBrowsableChildren(cacheCriteria);
                }
                pathExistsInCache = true;
            } catch (ItemNotFoundRuntimeException e) {
                // this is legit only if we also want to add remote items
                if (!includeRemoteResources) {
                    throw e;
                }
                log.trace("Local Repository Item Not Found", e);
            }
        }
        if (includeRemoteResources) {
            listRemoteBrowsableChildren(children, repo, relativePath, pathExistsInCache,updateRootNodesFilterFlag, rootNodesFilterResult);
        }
        Collections.sort(children);
        return children;
    }

    private void listRemoteBrowsableChildren(List<BaseBrowsableItem> children, RemoteRepo repo, String relativePath,
            boolean pathExistsInCache,boolean updateRootNodesFilterFlag, RootNodesFilterResult rootNodesFilterResult) {
        RepoPath repoPath = repo.getRepoPath(relativePath);
        List<RemoteItem> remoteItems = repo.listRemoteResources(relativePath);
        // probably remote not found - return 404 only if current folder doesn't exist in the cache
        if (remoteItems.isEmpty() && !pathExistsInCache) {
            // no cache and remote failed - signal 404
            log.trace("Couldn't find item:'{}'", repoPath);
            throw new ItemNotFoundRuntimeException("Couldn't find item: " + repoPath);
        }
        log.debug("Listing Remote Repo '{}' Browsable Items", repoPath.getRepoKey());

        // filter already existing local items
        remoteItems = Lists.newArrayList(
                Iterables.filter(remoteItems, new RemoteOnlyBrowsableItemPredicate(children)));
        for (RemoteItem remoteItem : remoteItems) {
            // remove the remote repository base url
            String path = StringUtils.removeStart(remoteItem.getUrl(), removeBaseUrl(repo.getUrl()));
            RepoPath remoteRepoPath = InternalRepoPathFactory.create(repoPath.getRepoKey(), path,
                    remoteItem.isDirectory());
            RepoPath cacheRepoPath = InternalRepoPathFactory.cacheRepoPath(remoteRepoPath);
            if (canRead(repo, cacheRepoPath)) {
                RemoteBrowsableItem browsableItem = new RemoteBrowsableItem(remoteItem, remoteRepoPath);
                if (remoteItem.getEffectiveUrl() != null) {
                    log.debug("Remote Browsable item effective URL", remoteItem.getEffectiveUrl());
                    browsableItem.setEffectiveUrl(remoteItem.getEffectiveUrl());
                }
                children.add(browsableItem);
            }
            else if (updateRootNodesFilterFlag && !authService.canRead(cacheRepoPath) ){
                rootNodesFilterResult.setAllItemNodesCanRead(false);
            }
        }
        if (updateRootNodesFilterFlag && !children.isEmpty()){
            rootNodesFilterResult.setAllItemNodesCanRead(true);
        }
    }

    private String removeBaseUrl(String originalUrl) {
        //If remote url ends with / it messes up the path builder, since we already add it below it's safe to remove
        String remoteUrl = PathUtils.trimTrailingSlashes(originalUrl);
        ArtifactoryStandardUrlResolver artifactoryStandardUrlResolver = new ArtifactoryStandardUrlResolver(remoteUrl);
        StringBuilder baseUrlBuilder = new StringBuilder(artifactoryStandardUrlResolver.getBaseUrl())
                .append("/").append(artifactoryStandardUrlResolver.getRepoKey());
        if (!remoteUrl.endsWith("/")) {
            baseUrlBuilder.append("/");
        }
        return baseUrlBuilder.toString();
    }

    @Override
    @Nonnull
    public List<BaseBrowsableItem> getVirtualRepoBrowsableChildren(BrowsableItemCriteria criteria) {
        return getVirtualRepoBrowsableChildrenData(criteria,false,null);
    }

    @Override
    @Nonnull
    public List<BaseBrowsableItem> getVirtualRepoBrowsableChildren(BrowsableItemCriteria criteria,
            boolean updateRootNodesFilterFlag,RootNodesFilterResult rootNodesFilterResult) {
        return getVirtualRepoBrowsableChildrenData(criteria, updateRootNodesFilterFlag,rootNodesFilterResult);
    }


    private List<BaseBrowsableItem> getVirtualRepoBrowsableChildrenData(BrowsableItemCriteria criteria,
            boolean updateRootNodesFilterFlag,RootNodesFilterResult rootNodesFilterResult) {
        RepoPath repoPath = criteria.getRepoPath();
        String virtualRepoKey = repoPath.getRepoKey();
        VirtualRepo virtualRepo = repoService.virtualRepositoryByKey(virtualRepoKey);
        if (virtualRepo == null) {
            log.trace("No virtual repo found:'{}'", virtualRepoKey);
            throw new IllegalArgumentException("No virtual repo found: " + virtualRepoKey);
        }
        log.debug("getting Virtual Repo '{}' Browsable Item ", virtualRepoKey);
        List<BaseBrowsableItem> candidateChildren = Lists.newArrayList();
        List<VirtualRepo> searchableRepos = getSearchableRepos(virtualRepo, repoPath);
        Multimap<String, VirtualRepo> pathToVirtualRepos = HashMultimap.create();
        // add children from all local repos

        getVirtualBrowsableItemFromLocalAndRemote(criteria, updateRootNodesFilterFlag,rootNodesFilterResult,
                candidateChildren, searchableRepos, pathToVirtualRepos);
        // only add the candidate that this virtual repository accepts via its include/exclude rules
        Map<String, BaseBrowsableItem> childrenToReturn = Maps.newHashMap();
        for (BaseBrowsableItem child : candidateChildren) {
            String childRelativePath = child.getRelativePath();
            if (virtualRepoAccepts(virtualRepo, child.getRepoPath())) {
                log.debug("Virtual Repo '{}' accepts child repo path '{}' ", virtualRepoKey, child.getRepoPath());
                VirtualBrowsableItem virtualItem;
                if (childrenToReturn.containsKey(childRelativePath)) {
                    virtualItem = (VirtualBrowsableItem) childrenToReturn.get(childRelativePath);
                    if (!child.isRemote() && virtualItem.isRemote()) {
                        virtualItem.setCreated(child.getCreated());
                        virtualItem.setLastModified(child.getLastModified());
                        virtualItem.setSize(child.getSize());
                        log.debug("Updating virtual Item '{}' created '{}' ,lastModified '{}' and size '{}'",
                                virtualItem.getName(),
                                child.getCreated(),
                                child.getLastModified(),
                                child.getSize());
                    }
                } else {
                    // New
                    Collection<VirtualRepo> virtualRepos = pathToVirtualRepos.get(childRelativePath);
                    virtualItem = new VirtualBrowsableItem(child.getName(), child.isFolder(), child.getCreated(),
                            child.getLastModified(), child.getSize(), InternalRepoPathFactory.create(virtualRepoKey,
                            childRelativePath, child.isFolder()),
                            Lists.newArrayList(getSearchableRepoKeys(virtualRepos))
                    );
                    log.debug("New virtual Item '{}' created '{}' ,lastModified '{}' and size '{}'",
                            virtualItem.getName(),
                            child.getCreated(),
                            child.getLastModified(),
                            child.getSize());

                    virtualItem.setRemote(true);    // default to true
                    childrenToReturn.put(childRelativePath, virtualItem);
                }
                virtualItem.setRemote(virtualItem.isRemote() && child.isRemote());   // remote if all are remote
                virtualItem.addRepoKey(child.getRepoKey());
            }
        }
        return Lists.newArrayList(childrenToReturn.values());
    }

    /**
     * get browsable children items from local repository and remote repository , if list return is empty due to no
     * permission to access nodes then BrowsableItemAccept will be update so a challenge could be send to UI
     * @param criteria browsing criteria
     * @param updateRootNodesFilterFlag - is require to update item list with permission to access flag
     * @param rootNodesFilterResult - object that hold list permission to access flag
     * @param candidateChildren - list of brows children
     * @param searchableRepos - repose to search
     * @param pathToVirtualRepos - path to virtual repo
     */
    private void getVirtualBrowsableItemFromLocalAndRemote(BrowsableItemCriteria criteria,
            boolean updateRootNodesFilterFlag, RootNodesFilterResult rootNodesFilterResult,
            List<BaseBrowsableItem> candidateChildren, List<VirtualRepo> searchableRepos,
            Multimap<String, VirtualRepo> pathToVirtualRepos) {
        StringBuilder canReadReposFlags = new StringBuilder();
        if (updateRootNodesFilterFlag) {
            for (VirtualRepo repo : searchableRepos) {
                RootNodesFilterResult localBrowsableItemAccept = new RootNodesFilterResult();
                RootNodesFilterResult remoteBrowsableItemAccept = new RootNodesFilterResult();
                addVirtualBrowsableItemsFromLocal(criteria, repo, candidateChildren, pathToVirtualRepos,
                        updateRootNodesFilterFlag,localBrowsableItemAccept);
                addVirtualBrowsableItemsFromRemote(criteria, repo, candidateChildren, pathToVirtualRepos,
                        updateRootNodesFilterFlag,remoteBrowsableItemAccept);
                updatefolderCanRead(canReadReposFlags, localBrowsableItemAccept, remoteBrowsableItemAccept);
            }
            if (canReadReposFlags.toString().indexOf("false") != -1 && candidateChildren.isEmpty() ){
                rootNodesFilterResult.setAllItemNodesCanRead(false);
            }
        }else {
            for (VirtualRepo repo : searchableRepos) {
                RootNodesFilterResult localBrowsableItemAccept = new RootNodesFilterResult();
                RootNodesFilterResult remoteBrowsableItemAccept = new RootNodesFilterResult();
                addVirtualBrowsableItemsFromLocal(criteria, repo, candidateChildren, pathToVirtualRepos,
                        updateRootNodesFilterFlag,localBrowsableItemAccept);
                addVirtualBrowsableItemsFromRemote(criteria, repo, candidateChildren, pathToVirtualRepos,
                        updateRootNodesFilterFlag,remoteBrowsableItemAccept);
            }
        }
    }

    /**
     * update repo folder can read
     * @param canReadReposFlags - update true / false flag for each repo that can or can not read
     * @param localBrowsableItemAccept - hold can read flag for local
     * @param remoteBrowsableItemAccept - hold can read flag for remote
     */
    private void updatefolderCanRead(StringBuilder canReadReposFlags, RootNodesFilterResult localBrowsableItemAccept,
            RootNodesFilterResult remoteBrowsableItemAccept) {
        if (hasAtLeastOneItemWithReadPermission(localBrowsableItemAccept, remoteBrowsableItemAccept)) {
            canReadReposFlags.append("true");
        }
        else{
            canReadReposFlags.append("false");
        }
    }

    private boolean hasAtLeastOneItemWithReadPermission(RootNodesFilterResult localBrowsableItemAccept,
            RootNodesFilterResult remoteBrowsableItemAccept) {
        return (localBrowsableItemAccept.isAllItemNodesCanRead() && remoteBrowsableItemAccept.isAllItemNodesCanRead());
    }

    private void addVirtualBrowsableItemsFromLocal(BrowsableItemCriteria criteria, VirtualRepo repo,
            List<BaseBrowsableItem> candidateChildren, Multimap<String, VirtualRepo> pathToVirtualRepos,
            boolean updateRootNodesFilterFlag ,RootNodesFilterResult rootNodesFilterResult) {
        String relativePath = criteria.getRepoPath().getPath();
        List<LocalRepo> localRepositories = repo.getLocalRepositories();
        log.debug("adding  Virtual Browsable Items From Local to virtual Repo:'{}'", repo);
        for (LocalRepo localRepo : localRepositories) {
            RepoPath path = InternalRepoPathFactory.create(localRepo.getKey(),
                            relativePath, criteria.getRepoPath().isFolder());
            try {
                BrowsableItemCriteria localCriteria = new BrowsableItemCriteria.Builder(criteria).repoPath(path).
                        build();
                log.trace("Iterating Browsable childrens of Local Repo :'{}' and check Virtual Repo Accepts it",
                        localRepo.getKey());
                List<BaseBrowsableItem> localRepoBrowsableChildren = getLocalBaseBrowsableItems(
                        updateRootNodesFilterFlag, rootNodesFilterResult, localCriteria);
                // go over all local repo browsable children, these have already been filtered according
                // to each local repo's rules, now all that is left is to check that the virtual repo that
                // the local repo belongs to accepts as well.
                for (BaseBrowsableItem localRepoBrowsableChild : localRepoBrowsableChildren) {
                    if (virtualRepoAccepts(repo, localRepoBrowsableChild.getRepoPath())) {
                        log.debug("virtual repo accept Local Repo browsable child '{}':'{}'", localRepo.getKey(),
                                localRepoBrowsableChild.getName());
                        pathToVirtualRepos.put(localRepoBrowsableChild.getRelativePath(), repo);
                        candidateChildren.add(localRepoBrowsableChild);
                    }
                }
            } catch (ItemNotFoundRuntimeException e) {
                log.trace("Could not find local browsable children at '{}'", criteria + " " + e.getMessage());
            }
        }
    }

    /**
     * call getLocalRepoBrowsableChildren with monitor emptyList acceptance Flag if true or
     * base getLocalRepoBrowsableChildren if not
     * @param updateRootNodesFilterFlag - monitor BaseBrowsableItem empty list due to missing read permission to Node
     * @param rootNodesFilterResult - Hold the missing read permission to Node flag if BaseBrowsableItem list is empty
     * @param localCriteria - criteria to find nodes
     * @return list of BaseBrowsableItem
     */
    private List<BaseBrowsableItem> getLocalBaseBrowsableItems(boolean updateRootNodesFilterFlag,
            RootNodesFilterResult rootNodesFilterResult, BrowsableItemCriteria localCriteria) {
        List<BaseBrowsableItem> localRepoBrowsableChildren;
        if (updateRootNodesFilterFlag) {
            localRepoBrowsableChildren = getLocalRepoBrowsableChildren(localCriteria,
                    true, rootNodesFilterResult);
        }else{
            localRepoBrowsableChildren = getLocalRepoBrowsableChildren(localCriteria);
        }
        return localRepoBrowsableChildren;
    }

    private void addVirtualBrowsableItemsFromRemote(BrowsableItemCriteria criteria, VirtualRepo repo,
            List<BaseBrowsableItem> candidateChildren, Multimap<String, VirtualRepo> pathToVirtualRepos,
            boolean isBrowsableItemsAccepted,RootNodesFilterResult browsableItemAccept) {
        List<RemoteRepo> remoteRepositories = repo.getRemoteRepositories();
        // add children from all remote repos (and their caches)
        log.debug("adding  Virtual Browsable Items From Remote to virtual Repo:'{}'", repo);
        for (RemoteRepo remoteRepo : remoteRepositories) {
            RepoPath remoteRepoPath = InternalRepoPathFactory.create(remoteRepo.getKey(),
                    criteria.getRepoPath().getPath(), criteria.getRepoPath().isFolder());
            try {
                BrowsableItemCriteria remoteCriteria = new BrowsableItemCriteria.Builder(criteria).
                        repoPath(remoteRepoPath).build();
                List<BaseBrowsableItem> remoteRepoBrowsableChildren;
                if (isBrowsableItemsAccepted) {
                     remoteRepoBrowsableChildren =
                            getRemoteRepoBrowsableChildren(remoteCriteria,true,browsableItemAccept);
                }else{
                    remoteRepoBrowsableChildren =
                            getRemoteRepoBrowsableChildren(remoteCriteria);
                }
                log.trace("Iterating Browsable childrens of Remote Repo :'{}' and check Virtual Repo Accepts it",
                        remoteRepo.getKey());
                for (BaseBrowsableItem remoteRepoBrowsableChild : remoteRepoBrowsableChildren) {
                    if (virtualRepoAccepts(repo, remoteRepoBrowsableChild.getRepoPath())) {
                        log.debug("virtual repo accept Remote Repo browsable child '{}':'{}'", remoteRepo.getKey(),
                                remoteRepoBrowsableChild.getName());
                        pathToVirtualRepos.put(remoteRepoBrowsableChild.getRelativePath(), repo);
                        candidateChildren.add(remoteRepoBrowsableChild);
                    }
                }
            } catch (ItemNotFoundRuntimeException e) {
                log.trace("Could not find local browsable children at '{}'",
                        criteria + " " + e.getMessage());
            }
        }
    }

    private List<VirtualRepo> getSearchableRepos(VirtualRepo virtualRepo, RepoPath pathToCheck) {
        log.debug("getting searchable repositories of virtual repo '{}'", virtualRepo.getKey());
        List<VirtualRepo> repos = Lists.newArrayList();
        List<VirtualRepo> allVirtualRepos = virtualRepo.getResolvedVirtualRepos();
        for (VirtualRepo repo : allVirtualRepos) {
            if (repo.accepts(pathToCheck)) {
                log.debug("repo '{}' accepts path '{}'", repo.getKey(), pathToCheck.getPath());
                repos.add(repo);
            }
        }
        return repos;
    }

    private Collection<String> getSearchableRepoKeys(Collection<VirtualRepo> virtualRepos) {
        log.debug("getting searchable repositories of virtual repo keys ");
        return Collections2.transform(virtualRepos, new Function<VirtualRepo, String>() {
            @Override
            public String apply(@Nonnull VirtualRepo input) {
                log.trace("searchable repo key '{}'", input.getKey());
                return input.getKey();
            }
        });
    }

    private boolean virtualRepoAccepts(VirtualRepo virtualRepo, RepoPath repoPath) {
        String path = repoPath.getPath();
        if (repoPath.isFolder()) {
            path += "/";
        }

        //If the path is not accepted, return immediately
        if (!virtualRepo.accepts(repoPath)) {
            log.debug("Virtual repo '{}' did not accept path '{}'", virtualRepo, repoPath);
            return false;
        }

        if (path.contains(MavenNaming.NEXUS_INDEX_DIR) || MavenNaming.isIndex(path)) {
            log.debug("Path '{}' is not an index", path);
            return false;
        }
        return true;
    }

    @Override
    public VirtualRepoItem getVirtualRepoItem(RepoPath repoPath) {
        VirtualRepo virtualRepo = repoService.virtualRepositoryByKey(repoPath.getRepoKey());
        if (virtualRepo == null) {
            log.trace("Repository '{}' does not exists!", repoPath.getRepoKey());
            throw new IllegalArgumentException("Repository " + repoPath.getRepoKey() + " does not exists!");
        }
        log.debug("getting virtual Repo '{}' item", repoPath.getRepoKey());
        VirtualRepoItem repoItem = virtualRepo.getVirtualRepoItem(repoPath);
        if (repoItem == null) {
            return null;
        }

        //Security - check that we can return the child
        Iterator<String> repoKeysIterator = repoItem.getRepoKeys().iterator();
        while (repoKeysIterator.hasNext()) {
            String realRepoKey = repoKeysIterator.next();
            RepoPath realRepoPath = InternalRepoPathFactory.create(realRepoKey, repoPath.getPath());
            boolean canRead = authService.canRead(realRepoPath);
            if (!canRead) {
                log.trace("removing repo '{}' item, not have read access permission", realRepoKey);
                //Don't bother with stuff that we do not have read access to
                repoKeysIterator.remove();
            }
        }

        // return null if user doesn't have permissions for any of the real repo paths
        if (repoItem.getRepoKeys().isEmpty()) {
            log.trace("user doesn't have permissions for any of the real repo paths");
            return null;
        } else {
            return repoItem;
        }
    }

    @Override
    public List<VirtualRepoItem> getVirtualRepoItems(RepoPath folderPath) {
        log.debug("getting Virtual Repo Items '{}'", folderPath.getRepoKey());
        VirtualRepo virtualRepo = repoService.virtualRepositoryByKey(folderPath.getRepoKey());
        if (virtualRepo == null) {
            log.trace("Repository '{}' does not exists!", folderPath.getRepoKey());
            throw new RepositoryRuntimeException(
                    "Repository " + folderPath.getRepoKey() + " does not exists!");
        }
        //Get a deep children view of the virtual repository (including contained virtual repos)
        Set<String> children = virtualRepo.getChildrenNamesDeeply(folderPath);
        List<VirtualRepoItem> result = new ArrayList<>(children.size());
        for (String childName : children) {
            //Do not add or check hidden items
            RepoPath childPath = InternalRepoPathFactory.create(folderPath, childName);
            VirtualRepoItem virtualRepoItem = getVirtualRepoItem(childPath);
            log.debug("getting Virtual Repo Item for folder path '{}' and child '{}'", folderPath.getRepoKey(),
                    childName);
            if (virtualRepoItem != null) {
                result.add(virtualRepoItem);
            }
        }
        return result;
    }

    /**
     * Returns local-repo browsable items of checksums for the given browsable item
     *
     * @param repo          Browsed repo
     * @param checksumsInfo Item's checksum info
     * @param browsableItem Browsable item to create checksum items for    @return Checksum browsable items
     */
    private List<BrowsableItem> getBrowsableItemChecksumItems(LocalRepo repo,
            ChecksumsInfo checksumsInfo, BrowsableItem browsableItem) {
        log.debug("getting Local repo Browsable Checksum Items for LocalRepo '{}'", repo.getKey());
        List<BrowsableItem> browsableChecksumItems = Lists.newArrayList();
        Set<ChecksumInfo> checksums = checksumsInfo.getChecksums();
        for (ChecksumType checksumType : ChecksumType.BASE_CHECKSUM_TYPES) {
            String checksumValue = repo.getChecksumPolicy().getChecksum(checksumType, checksums);
            if (org.apache.commons.lang.StringUtils.isNotBlank(checksumValue)) {
                BrowsableItem checksumItem = BrowsableItem.getChecksumItem(browsableItem, checksumType,
                        checksumValue.getBytes(Charsets.UTF_8).length);
                log.debug("getting Local repo Browsable Checksum Item '{}':'{}'", repo.getKey(),
                        checksumItem.getName());

                RepoPath checksumItemRepoPath = checksumItem.getRepoPath();
                if (authService.canRead(checksumItemRepoPath) && repo.accepts(checksumItemRepoPath)) {
                    browsableChecksumItems.add(checksumItem);
                }
            }
        }

        return browsableChecksumItems;
    }

    /**
     * This predicate returns true if a given item, represented by URL, doesn't already exists in the local items.
     */
    private static class RemoteOnlyBrowsableItemPredicate implements Predicate<RemoteItem> {
        private List<BaseBrowsableItem> localItems;

        private RemoteOnlyBrowsableItemPredicate(List<BaseBrowsableItem> localItems) {
            this.localItems = localItems;
        }

        @Override
        public boolean apply(@Nonnull RemoteItem input) {
            for (BaseBrowsableItem localItem : localItems) {
                if (localItem.getName().equals(input.getName())) {
                    return false;
                }
            }
            return true;
        }
    }
}
