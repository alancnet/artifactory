package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.BaseBrowsableItem;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.FileInfo;
import org.artifactory.md.Properties;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.BaseArtifact;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.IAction;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.IArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.VirtualRemoteFileGeneralArtifactInfo;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Chen Keinan
 */
@JsonTypeName("virtualRemoteFile")
public class VirtualRemoteFileNode extends BaseNode {

    private FileInfo fileInfo;
    private String type = "virtualRemoteFile";
    private String mimeType;
    private BaseBrowsableItem browsableItem;
    private boolean cached;

    VirtualRemoteFileNode() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public VirtualRemoteFileNode(BaseBrowsableItem fileInfo, String text, String repoType) {
        super(fileInfo.getRepoPath());
        initFileNode(fileInfo, text, repoType);
        AuthorizationService authorizationService = ContextHelper.get().getAuthorizationService();
        populateActions(authorizationService);
        populateTabs(authorizationService);
    }

    /**
     * init virtual remote file node
     *
     * @param fileInfo - file item
     * @param text     - file text
     * @param repoType - repository type
     */
    private void initFileNode(BaseBrowsableItem fileInfo, String text, String repoType) {
        setLocal(false);
        setRepoType(repoType);
        super.setText(text);
        browsableItem = fileInfo;
        cached = !fileInfo.isRemote();
    }

    @Override
    public void populateTabs(AuthorizationService authorizationService) {
        List<IArtifactInfo> tabs = new ArrayList<>();
        addVirtualRemoteGeneralTab(tabs);
        super.setTabs(tabs);
    }

    /**
     * add virtual remote general tab info
     *
     * @param tabs
     */
    private void addVirtualRemoteGeneralTab(List<IArtifactInfo> tabs) {
        VirtualRemoteFileGeneralArtifactInfo general = new VirtualRemoteFileGeneralArtifactInfo("General");
        general.populateVirtualRemoteFileInfo(browsableItem);
        tabs.add(general);
    }

    @Override
    public void populateActions(AuthorizationService authService) {
        List<IAction> actions = new ArrayList<>();
        // add actions
        addDownloadAction(actions);
        setActions(actions);
    }

    /**
     * add download actions
     *
     * @param actions - actions list
     */
    private void addDownloadAction(List<IAction> actions) {
        actions.add(new BaseArtifact("Download"));
    }


    @Override
    protected RepoPath fetchRepoPath() {
        return fileInfo.getRepoPath();
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }


    @Override
    public List<? extends RestTreeNode> getChildren(AuthorizationService authService, boolean isCompact,
            ArtifactoryRestRequest request) {
        List<INode> childNodeList = new ArrayList<>();
        childNodeList.add(this);
        return childNodeList;
    }


    @Override
    public void updateNodeData() {
        mimeType = NamingUtils.getMimeType(this.getPath()).getType();
    }

    protected void updateHasChild(boolean hasChild) {
        super.setHasChild(hasChild);
    }

    @Override
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
