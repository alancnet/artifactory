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

import org.artifactory.addon.AddonType;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.md.Properties;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.BaseArtifact;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.IAction;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.RefreshArtifact;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.IArtifactInfo;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Chen Keinan
 */
@JsonTypeName("folder")
public class FolderNode extends BaseNode {

    private FolderInfo folderInfo;
    private String type = "folder";
    private boolean compacted;

    public String getType() {
        return type;
    }

    public void setFolderInfo(FolderInfo folderInfo) {
        this.folderInfo = folderInfo;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isCompacted() {
        return compacted;
    }

    public FolderInfo fetchFolderInfo() {
        return this.folderInfo;
    }

    public void setCompacted(boolean compacted) {
        this.compacted = compacted;
        updateNodeIcon();
    }

    FolderNode() {
    }

    public FolderNode(FolderInfo folderInfo, String text) {
        super(folderInfo.getRepoPath());
        this.folderInfo = folderInfo;
        this.setLocal(true);
        setRepoType("local");
        // update node display name
        updateNodeDisplayName(text);
        updateNodeIcon();
    }

    private void updateNodeIcon() {
        if (compacted) {
            if (isDockerCompactedFolder()) {
                super.setIcon("docker");
            }
        } else {
            if (isDockerManifestFolder()) {
                super.setIcon("docker");
            }
        }
    }

    /**
     * update node display name
     *
     * @param text - node orig name
     */
    private void updateNodeDisplayName(String text) {
        if (isLocal() && isDockerFileTypeAndSupported()) {
            super.setText(text.substring(0, 12));
        } else {
            super.setText(text);
        }
    }

    @Override
    public List<? extends RestTreeNode> getChildren(AuthorizationService authService, boolean isCompact,
            ArtifactoryRestRequest request) {
        List<INode> childNodeList = new ArrayList<>();
        childNodeList.add(this);
        return childNodeList;
    }

    @Override
    public void populateActions(AuthorizationService authService) {
        if (isLocal()) {
            RepoPath repoPath = InternalRepoPathFactory.create(getRepoKey(), getPath());
            boolean canRead = authService.canRead(repoPath);
            boolean canDelete = authService.canDelete(repoPath);
            boolean canAdmin = authService.canManage(repoPath);
            boolean isAnonymous = authService.isAnonymous();
            createFolderInfo();
            List<IAction> actions = new ArrayList<>();
            // add specific actions
            addDownloadAction(actions, isAnonymous, canRead);
            addRefreshAction(actions);
            addCopyAction(authService, actions, repoPath);
            addMoveAction(authService, actions, repoPath, canDelete);
            addWatchAction(authService, actions, canRead);
            addZapAction(actions, repoPath, canAdmin);
            addDeleteVersionAction(actions, repoPath, canRead, canDelete, canAdmin);
            addDeleteAction(actions, canDelete);
            setActions(actions);
        }
    }

    @Override
    public void populateTabs(AuthorizationService authService) {
        if (isLocal()) {
            List<IArtifactInfo> tabs = new ArrayList<>();
            boolean canAdminRepoPath = authService.canManage(getRepoPath());
            addGeneralTab(tabs);
            addDockerTab(tabs);
            addDockerV2Tab(tabs);
            addEffectivePermissionTab(tabs, canAdminRepoPath);
            addPropertiesTab(tabs);
            addWatchTab(tabs, canAdminRepoPath);
            setTabs(tabs);
        }
    }


    /**
     * add Docker tab
     *
     * @param tabs          - tabs list
     */
    private void addDockerTab(List<IArtifactInfo> tabs) {
        boolean isDockerFileTypeAndSupported = isDockerFileTypeAndSupported();
        if (isDockerFileTypeAndSupported) {
            tabs.add(new BaseArtifactInfo("DockerInfo"));
            tabs.add(new BaseArtifactInfo("DockerAncestryInfo"));
        }
    }

    private void addDockerV2Tab(List<IArtifactInfo> tabs) {
        if (isDockerManifestFolder()) {
            tabs.add(new BaseArtifactInfo("DockerV2Info"));
        }
    }

    /**
     * if true docker file is supported
     *
     * @return
     */
    private boolean isDockerFileTypeAndSupported() {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        LocalRepoDescriptor localRepoDescriptor = localOrCachedRepoDescriptor(getRepoPath());
        if (localRepoDescriptor == null) {
            return false;
        }
        boolean isDockerEnabled = RepoType.Docker.equals(localRepoDescriptor.getType()) &&
                addonsManager.isAddonSupported(AddonType.DOCKER);
        return isDockerEnabled && getRepoService().getChildrenNames(folderInfo.getRepoPath()).contains("json.json");
    }

    private boolean isDockerCompactedFolder() {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        LocalRepoDescriptor localRepoDescriptor = localOrCachedRepoDescriptor(getRepoPath());
        if (localRepoDescriptor == null) {
            return false;
        }
        boolean isDockerEnabled = RepoType.Docker.equals(localRepoDescriptor.getType()) &&
                addonsManager.isAddonSupported(AddonType.DOCKER);
        return isDockerEnabled && getRepoService().getChildrenNames(folderInfo.getRepoPath()).contains("manifest.json");
    }

    private boolean isDockerManifestFolder() {
        return isDockerCompactedFolder() &&
                getRepoService().getChildrenNames(folderInfo.getRepoPath()).contains("manifest.json");
    }


    @Override
    protected RepoPath fetchRepoPath() {
        return folderInfo.getRepoPath();
    }

    /**
     * create folder info if not exist
     */
    private void createFolderInfo() {
        if (folderInfo == null) {
            super.setRepoPath(InternalRepoPathFactory.create(getRepoKey(), getPath()));
            folderInfo = getRepoService().getFolderInfo(getRepoPath());
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
     * add refresh action
     *
     * @param actions - actions list
     */
    private void addRefreshAction(List<IAction> actions) {
        actions.add(new RefreshArtifact("Refresh"));
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

    public String toString() {
        return JsonUtil.jsonToString(this);
    }

    /**
     * fetch next child for compact
     * @return folder child
     */
    public FolderNode fetchNextChild(){
        RepositoryService repoService = getRepoService();
        // create repo path
        RepoPath repositoryPath = InternalRepoPathFactory.create(getRepoKey(),getPath());
        // get child's from repo service
        List<ItemInfo> items = repoService.getChildren(repositoryPath);
        if (items.size() != 1){
            return null;
        }
        // is only item is folder
        ItemInfo singleItem = items.get(0);
        if (!singleItem.isFolder()){
            return null;
        }
        return new FolderNode((FolderInfo)singleItem,singleItem.getName());
    }

    @Override
    public List<RestModel> fetchItemTypeData(AuthorizationService authService, boolean isCompact, Properties props,
            ArtifactoryRestRequest request) {
        Collection<? extends RestTreeNode> items = getChildren(authService, isCompact, request);
        List<RestModel> treeModel = new ArrayList<>();
        items.forEach(item -> {
            ((INode) item).populateActions(authService);
            // populate tabs
            ((INode) item).populateTabs(authService);
            // update additional data
            ((INode) item).updateNodeData();
            treeModel.add(item);
        });
        return treeModel;
    }
}
