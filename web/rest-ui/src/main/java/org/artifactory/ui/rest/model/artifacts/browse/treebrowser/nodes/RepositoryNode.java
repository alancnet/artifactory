package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import org.apache.commons.io.IOUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.ItemInfo;
import org.artifactory.md.Properties;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.BaseArtifact;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.IAction;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.RefreshArtifact;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.archive.ArchiveEntriesTree;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.archive.ArchiveTreeNode;
import org.artifactory.ui.utils.RegExUtils;
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
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Chen  Keinan
 */
@JsonTypeName("repository")
@JsonIgnoreProperties("repoPath")
public class RepositoryNode extends BaseNode {

    private static final Logger log = LoggerFactory.getLogger(JunctionNode.class);
    private String type = "repository";
    private RepoType repoPkgType;
    public RepositoryNode(RepoBaseDescriptor repo,String repoType) {
        super(InternalRepoPathFactory.create(repo.getKey(), ""));
        setRepoType(repoType);
        RepoPath repoPath = InternalRepoPathFactory.create(repo.getKey(), "");
        super.setText(repoPath.getRepoKey());
        updateLocalFlag();
        repoPkgType = repo.getType();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Collection<? extends RestTreeNode> getChildren(AuthorizationService authService, boolean isCompact,
            ArtifactoryRestRequest request) {
        List<INode> childNodeList = new ArrayList<>();
        childNodeList.add(this);
        return childNodeList;
    }

    /**
     * get archive children nodes
     *
     * @param authService - authorization service
     * @param compacted   - is compact mode
     * @return List or archive nodes
     */
    public Collection<? extends RestModel> getArchiveChildren(AuthorizationService authService, boolean compacted) {
        // create archive tree data
        ArchiveEntriesTree tree = buildArchiveEntriesTree();
        // fetch root element
        RestModel root = tree.getRoot();
        if (!((ArchiveTreeNode) root).hasChildren()) {
            return Collections.emptyList();
        }
        // create archive nodes from main root elements
        Collection<RestModel> items = getArchiveNodes(root);
        return items;
    }

    /**
     * create archive nodes from tree data
     *
     * @param root
     * @return
     */
    private Set<RestModel> getArchiveNodes(RestModel root) {
        return ((ArchiveTreeNode) root).getChildren();
    }

    /**
     * build archive entries tree from archive data
     *
     * @return archive entries tree instance
     */
    private ArchiveEntriesTree buildArchiveEntriesTree() {
        ArchiveEntriesTree tree = new ArchiveEntriesTree();
        String repoKey = getRepoKey();
        ZipInputStream zipInputStream = null;
        try {
            ZipEntry zipEntry;
            // create repo path
            RepoPath repositoryPath = InternalRepoPathFactory.create(repoKey, getPath());
            zipInputStream = getRepoService().zipInputStream(repositoryPath);
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                tree.insert(InfoFactoryHolder.get().createZipEntry(zipEntry), repoKey, getPath());
            }
        } catch (IOException e) {
            log.error("Failed to get  zip Input Stream: " + e.getMessage());
        } finally {
            if (zipInputStream != null) {
                IOUtils.closeQuietly(zipInputStream);
            }
            return tree;
        }
    }

    @Override
    public void populateActions(AuthorizationService authService) {
        List<IAction> actions = new ArrayList<>();
        // update repo path and auth data
        RepoPath repoPath = getRepoPath();
        boolean canDelete = authService.canDelete(repoPath);
        boolean canRead = authService.canRead(repoPath);
        boolean canManage = authService.canManage(repoPath);
        boolean isAnonymous = authService.isAnonymous();
        // add specific actions
        addDownloadAction(actions, isAnonymous, canRead);
        addRefreshAction(actions);
        addCopyAction(authService, actions, repoPath);
        addMoveAction(authService, actions, repoPath, canDelete);
        addWatchAction(authService, actions, canRead);
        addZapAction(actions, repoPath, authService.canManage(repoPath));
        addPackageReindexAction(actions, authService.isAdmin());
        addDeleteVersionAction(actions, repoPath, canRead, canDelete, canManage);
        addDeleteAction(actions, canDelete);
        setActions(actions);
    }

    /**
     * add package reindex action for repositories which support it
     *
     * @param actions - actions list
     */
    private void addPackageReindexAction(List<IAction> actions, boolean isAdmin) {
        Matcher matcher = RegExUtils.LOCAL_REPO_REINDEX_PATTERN.matcher(this.repoPkgType.name());
        boolean foundMatch = matcher.matches();
        if (isAdmin && foundMatch && getRepoType().equals("local")) {
            actions.add(new BaseArtifact("RecalculateIndex"));
        }
    }

    @Override
    protected RepoPath fetchRepoPath() {
        return super.getRepoPath();
    }

    /**
     * add refresh action
     *
     * @param actions - action list
     */
    private void addRefreshAction(List<IAction> actions) {
        actions.add(new RefreshArtifact("Refresh"));
    }

    /**
     * populate copy action
     *
     * @param authService - authorization service
     * @param actions     - list of actions
     * @param repoPath    - repo path
     * @return if true - has can  read permission
     */
    @Override
    protected boolean addCopyAction(AuthorizationService authService, List<IAction> actions, RepoPath repoPath) {
        boolean canRead = authService.canRead(repoPath);
        if (canRead && !NamingUtils.isSystem(repoPath.getPath()) && authService.canDeployToLocalRepository()) {
            actions.add(new BaseArtifact("CopyContent"));
        }
        return canRead;
    }

    /**
     * populate MoveArtifact
     *
     * @param authService - authorization service
     * @param actions     - list of actions
     * @param repoPath    - repo path
     * @return if true - has can  read permission
     */

    @Override
    protected void addMoveAction(AuthorizationService authService, List<IAction> actions, RepoPath repoPath,
            boolean canDelete) {
        if (canDelete && !NamingUtils.isSystem(repoPath.getPath()) && authService.canDeployToLocalRepository()) {
            actions.add(new BaseArtifact("MoveContent"));
        }
    }

    /**
     * add delete version action
     *
     * @param path        - path
     * @param canDelete   - user has delete permissions on path?
     * @param actions     - actions list
     */
    private void addDeleteVersionAction(List<IAction> actions, RepoPath path, boolean canRead, boolean canDelete,
            boolean canManage) {
        if ((canManage || canDelete) && canRead && localOrCachedRepoDescriptor(path).isLocal()) {
            actions.add(new BaseArtifact("DeleteVersions"));
        }
    }

    /**
     * populate delete action
     *
     * @param actions   - list of actions
     * @param canDelete - if true can delete
     */
    @Override
    protected void addDeleteAction(List<IAction> actions, boolean canDelete) {
        if (canDelete) {
            actions.add(new BaseArtifact("DeleteContent"));
        }
    }

    /**
     * add download action
     *
     * @param actions - actions list
     */
    private void addDownloadAction(List<IAction> actions, boolean isAnonymous, boolean canRead) {
        if(!isAnonymous && isLocal() && canRead && ContextHelper.get().beanForType(CentralConfigService.class)
                .getDescriptor().getFolderDownloadConfig().isEnabled()) {
            actions.add(new BaseArtifact("DownloadFolder"));
        }
    }

    @Override
    public Collection<? extends RestModel> fetchItemTypeData(AuthorizationService authService, boolean isCompact,
            Properties props, ArtifactoryRestRequest request) {
        if (isArchive()) {
            // get all archive children
            return getArchiveChildren(authService, isCompact);
        } else {
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

    private void updateLocalFlag(){
        switch (repoType){
            case "remote" : {
                setLocal(false);
                setHasChild(true);
            }
                break;
            case "virtual" : {
                setLocal(false);
                setHasChild(true);
            }
                break;
            case "cached" : {
                setLocal(true);
            }
                break;
            case "local" : setLocal(true);
                break;
        }
    }

    public RepoType getRepoPkgType() {
        return repoPkgType;
    }

    public void setRepoPkgType(RepoType repoPkgType) {
        this.repoPkgType = repoPkgType;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
