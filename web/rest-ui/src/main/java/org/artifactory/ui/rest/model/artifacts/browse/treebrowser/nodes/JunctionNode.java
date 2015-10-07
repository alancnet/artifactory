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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import com.google.common.collect.Lists;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.BaseBrowsableItem;
import org.artifactory.api.repo.BrowsableItemCriteria;
import org.artifactory.api.repo.RepositoryBrowsingService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.md.Properties;
import org.artifactory.mime.MimeType;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.archive.ArchiveEntriesTree;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.archive.ArchiveTreeNode;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Chen  Keinan
 */
@JsonTypeName("junction")
@JsonIgnoreProperties("repoPath")
public class JunctionNode implements RestTreeNode {
    private static final Logger log = LoggerFactory.getLogger(JunctionNode.class);
    private String repoKey;
    private String path;
    private String repoType;
    private Properties props;


    @Override
    public Collection<? extends RestTreeNode> getChildren(AuthorizationService authService, boolean isCompact,
            ArtifactoryRestRequest request) {
        List<RestTreeNode> children;
        // create repo path
        RepoPath repositoryPath = InternalRepoPathFactory.create(getRepoKey(), getPath());
        RepositoryService repoService = getRepoService();
        BrowsableItemCriteria criteria;
        List<ItemInfo> items;
        // get child's from repo service
        RepositoryBrowsingService repositoryBrowsingService = ContextHelper.get().beanForType(RepositoryBrowsingService.class);
        switch (repoType) {
            case "local":
                items = repoService.getChildren(repositoryPath);
                children = Lists.newArrayListWithExpectedSize(items.size());
                // populate child data
                populateChildData(children, repoService, items, isCompact);
                break;
            case "cache": {
                items = repoService.getChildren(repositoryPath);
                children = Lists.newArrayListWithExpectedSize(items.size());
                // populate child data
                populateChildData(children, repoService, items, isCompact);
                break;
            }
            case "remote": {
                RepoPath remoteRepoPath = InternalRepoPathFactory.create(getRepoKey(), getPath(), true);
                criteria = getBrowsableItemCriteria(remoteRepoPath);
                List<BaseBrowsableItem> remoteChildren = repositoryBrowsingService.getRemoteRepoBrowsableChildren(criteria);
                children = Lists.newArrayListWithExpectedSize(remoteChildren.size());
                Collections.sort(remoteChildren);
                populateRemoteData(children, remoteChildren, "remote");
                break;
            }
            case "virtual": {
                RepoPath virtualRepoPath = InternalRepoPathFactory.create(getRepoKey(), getPath(), true);
                criteria = getBrowsableItemCriteria(virtualRepoPath);
                List<BaseBrowsableItem> virtualChildren = repositoryBrowsingService.getVirtualRepoBrowsableChildren(criteria);
                children = Lists.newArrayListWithExpectedSize(virtualChildren.size());
                Collections.sort(virtualChildren);
                populateRemoteData(children, virtualChildren, "virtual");
                break;
            }
            default: {
                items = repoService.getChildren(repositoryPath);
                children = Lists.newArrayListWithExpectedSize(items.size());
                // populate child data
                populateChildData(children, repoService, items, isCompact);
                break;
            }
        }
        return children;
}

    /**
     * get remote and virtual browsable item criteria
     *
     * @param repositoryPath - repository path
     * @return browsable item criteria
     */
    private BrowsableItemCriteria getBrowsableItemCriteria(RepoPath repositoryPath) {
        return new BrowsableItemCriteria.Builder(repositoryPath)
                .requestProperties(props)
                .includeChecksums(false).build();
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

    /**
     * get archive children nodes
     * @param authService - authorization service
     * @param compacted - is compact mode
     * @return List or archive nodes
     */
    public Collection<? extends RestModel> getArchiveChildren(AuthorizationService authService, boolean compacted) {
        // create archive tree data
        ArchiveEntriesTree  tree = buildArchiveEntriesTree();
        // fetch root element
        RestModel root = tree.getRoot();
            if (!((ArchiveTreeNode)root).hasChildren()) {
                return Collections.emptyList();
            }
        // create archive nodes from main root elements
        Collection<RestModel> items = getArchiveNodes(root);
        return items;
    }

    /**
     * create archive nodes from tree data
     * @param root
     * @return
     */
    private Set<RestModel> getArchiveNodes(RestModel root) {
         return  ((ArchiveTreeNode) root).getChildren();
    }

    /**
     * build archive entries tree from archive data
     * @return archive entries tree instance
     */
    private ArchiveEntriesTree buildArchiveEntriesTree() {
        ArchiveEntriesTree tree = new ArchiveEntriesTree();
        String repoKey = getRepoKey();
        ArchiveInputStream archiveInputStream = null;
        try {
            ArchiveEntry archiveEntry;
            // create repo path
            RepoPath repositoryPath = InternalRepoPathFactory.create(repoKey, getPath());
            archiveInputStream = getRepoService().archiveInputStream(repositoryPath);
            while ((archiveEntry = archiveInputStream.getNextEntry()) != null) {
                tree.insert(InfoFactoryHolder.get().createArchiveEntry(archiveEntry), repoKey, getPath());
            }
        } catch (IOException e) {
            log.error("Failed to get  zip Input Stream: " + e.getMessage());
        } finally {
            if (archiveInputStream != null) {
                IOUtils.closeQuietly(archiveInputStream);
            }
            return tree;
        }
    }

    /**
     * populate File or Folder Data
     * @param children - list of child's
     * @param repoService - repository service
     * @param items - nodes items ( File or Folder)
     */
    private void populateChildData(List<RestTreeNode> children, RepositoryService repoService, List<ItemInfo> items,
            boolean isCompact) {
        for (ItemInfo pathItem : items) {
            RepoPath repoPath = pathItem.getRepoPath();
            if (!repoService.isRepoPathVisible(repoPath)) {
                continue;
            }
            children.add(getChildItem(pathItem, pathItem.getRelPath(),repoPath,isCompact));
        }
    }

    /**
     * populate File or Folder Data
     * @param children - list of child's
     * @param items - nodes items ( File or Folder)
     */
    private void populateRemoteData(List<RestTreeNode> children,
            List<BaseBrowsableItem> items, String repoType) {
        for (BaseBrowsableItem pathItem : items) {
            children.add(getRemoteChildItem(pathItem, repoType));
        }
    }

    /**
     * Returns a new child  node item
     *
     * @param pathItem       The path to the child content
     * @param relativePath   The relative path to the child itself
     * @return File or folder node
     */
    protected RestTreeNode getChildItem(ItemInfo pathItem, String relativePath, RepoPath repoPath, boolean isCompact) {
        RestTreeNode child;
        if (pathItem.isFolder()) {
            child = new FolderNode(((FolderInfo) pathItem),pathItem.getName());
            if  (isCompact){
                // compact child folder
                compactFolder(child);
           }
            ((FolderNode) child).setHasChild(getRepoService().hasChildren(repoPath));
        } else {
            MimeType mimeType = NamingUtils.getMimeType(relativePath);
            if (mimeType.isArchive() /*|| relativePath.endsWith("tar") || relativePath.endsWith("tar.gz")
                    || relativePath.endsWith("tgz")*/) {
                child =  new ZipFileNode((FileInfo) pathItem,pathItem.getName());
                ((FileNode) child).setHasChild(true);
            } else {
                child = new FileNode ((FileInfo) pathItem,pathItem.getName());
            }
        }
        return child;
    }

    /**
     * Returns a new child  node item
     *
     * @param pathItem       The path to the child content
     * @return File or folder node
     */
    protected RestTreeNode getRemoteChildItem(BaseBrowsableItem pathItem, String repoType) {
        RestTreeNode child;
        RepoPath repositoryPath = null;
        if (pathItem.isFolder()) {
            child = createRemoteOrVirtualFolderNode(pathItem, repoType, repositoryPath);

        } else {
            child = new VirtualRemoteFileNode(pathItem, pathItem.getName(), repoType);
        }
        return child;
    }

    /**
     * create remote or virtual folder node
     *
     * @param pathItem       - path item
     * @param repoType       - repo type
     * @param repositoryPath - repository path
     * @return - tree node
     */
    private RestTreeNode createRemoteOrVirtualFolderNode(BaseBrowsableItem pathItem, String repoType,
            RepoPath repositoryPath) {
        RestTreeNode child;
        String repoKey = pathItem.getRepoKey();
        repositoryPath = pathItem.getRepoPath();
        if (repoKey.endsWith("-cache")) {
            repoKey = repoKey.replace("-cache", "");
            repositoryPath = InternalRepoPathFactory.create(repoKey, pathItem.getRepoPath().getPath());
        }
        child = new VirtualRemoteFolderNode(repositoryPath, pathItem, pathItem.getName(), repoType);
        return child;
    }

    /**
     * compact 1sr folder child  by looking for empty child folders
     * @param child - 1st folder child
     */
    private void compactFolder(RestTreeNode child) {

        FolderNode folder = (FolderNode) child;
        StringBuilder nameBuilder = new StringBuilder(folder.getText());
        FolderNode next = folder.fetchNextChild();
        // look for empty folders
        while (next != null){
            folder.setCompacted(true);
            folder.setFolderInfo(next.fetchFolderInfo());
            folder.setRepoPath(next.getRepoPath());
            // update compact name
            nameBuilder.append('/').append(next.getText());
            next = next.fetchNextChild();
        }
        folder.setText(nameBuilder.toString());
    }

    @Override
    public Collection<? extends RestModel> fetchItemTypeData(AuthorizationService authService,
            boolean isCompact, Properties props, ArtifactoryRestRequest request) {
        if ((repoType.equals("local") || repoType.equals("cached")) && isArchive() /*|| getPath().endsWith("tar") ||
                getPath().endsWith("tar.gz") || getPath().endsWith("tgz")*/) {
            // get all archive children
            return getArchiveChildren(authService, isCompact);
        } else {
            this.props = props;
            // get repository or folder children 1st depth
            return getRepoOrFolderChildren(authService, isCompact, request);
        }
    }

    /**
     * get repository or folder children
     *
     * @param authService - authorization service
     * @param isCompact   - is compacted
     * @param request
     * @return
     */
    private Collection<? extends RestModel> getRepoOrFolderChildren(AuthorizationService authService, boolean isCompact,
            ArtifactoryRestRequest request) {
        Collection<? extends RestTreeNode> items = getChildren(authService, isCompact, request);
        List<RestModel> treeModel = new ArrayList<>();
        items.forEach(item -> {
            // update additional data
            ((INode) item).updateNodeData();
            treeModel.add(item);
        });
        return treeModel;
    }

    /**
     * check if folder type is archive
     *
     * @return if true , folder type is archive
     */
    private boolean isArchive() {
        RepoPath repositoryPath = InternalRepoPathFactory.create(getRepoKey(), getPath());
        ItemInfo fileInfo = retrieveItemInfo(repositoryPath);
        return NamingUtils.getMimeType(fileInfo.getRelPath()).isArchive();
    }

    protected RepositoryService getRepoService() {
        return ContextHelper.get().beanForType(RepositoryService.class);
    }

    /**
     * retrieve item info
     *
     * @param repoPath - repo path
     * @return - item info
     */
    public org.artifactory.fs.ItemInfo retrieveItemInfo(RepoPath repoPath) {
        return getRepoService().getItemInfo(repoPath);
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }
}
