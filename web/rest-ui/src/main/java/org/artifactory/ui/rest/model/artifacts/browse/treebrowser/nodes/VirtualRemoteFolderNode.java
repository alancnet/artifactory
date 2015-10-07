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

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.BaseBrowsableItem;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.FolderInfo;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.IAction;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.RefreshArtifact;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.IArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.VirtualRemoteFolderGeneralArtifactInfo;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Chen Keinan
 */
@JsonTypeName("virtualRemoteFolder")
public class VirtualRemoteFolderNode extends BaseNode {

    private FolderInfo folderInfo;
    private String type = "virtualRemoteFolder";
    private boolean compacted;
    private BaseBrowsableItem pathItem;
    private boolean cached;


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
    }

    VirtualRemoteFolderNode() {
    }

    public VirtualRemoteFolderNode(RepoPath repoPath, BaseBrowsableItem pathItem, String text,
            String repoType) {
        super(repoPath);
        initFolderNode(pathItem, text, repoType);
        AuthorizationService authorizationService = ContextHelper.get().getAuthorizationService();
        populateActions(authorizationService);
        populateTabs(authorizationService);
        cached = !pathItem.isRemote();
    }


    /**
     * initialize folder node
     *
     * @param pathItem - item
     * @param text     - item text
     * @param repoType - repository type
     */
    private void initFolderNode(BaseBrowsableItem pathItem, String text, String repoType) {
        this.setLocal(false);
        setRepoType(repoType);
        setHasChild(pathItem.isFolder());
        updateNodeDisplayName(text);
        this.pathItem = pathItem;
    }

    /**
     * update node display name
     *
     * @param text - node orig name
     */
    private void updateNodeDisplayName(String text) {
        super.setText(text);
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
        List<IAction> actions = new ArrayList<>();
        addRefreshAction(actions);
        setActions(actions);
    }

    /**
     * add refresh action
     *
     * @param actions - actions list
     */
    private void addRefreshAction(List<IAction> actions) {
        actions.add(new RefreshArtifact("Refresh"));
    }

    @Override
    public void populateTabs(AuthorizationService authService) {
        List<IArtifactInfo> tabs = new ArrayList<>();
        VirtualRemoteFolderGeneralArtifactInfo general = new VirtualRemoteFolderGeneralArtifactInfo("General");
        general.populateGeneralData(pathItem);
        tabs.add(general);
        super.setTabs(tabs);
    }

    @Override
    protected RepoPath fetchRepoPath() {
        return folderInfo.getRepoPath();
    }


    public String toString() {
        return JsonUtil.jsonToString(this);
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

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }
}
