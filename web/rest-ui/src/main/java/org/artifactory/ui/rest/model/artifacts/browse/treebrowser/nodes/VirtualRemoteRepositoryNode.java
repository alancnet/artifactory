package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoType;
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
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.VirtualRemoteRepoGeneralArtifactInfo;
import org.artifactory.ui.utils.RegExUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Chen  Keinan
 */
@JsonTypeName("virtualRemoteRepository")
@JsonIgnoreProperties("repoPath")
public class VirtualRemoteRepositoryNode extends BaseNode {

    private static final Logger log = LoggerFactory.getLogger(JunctionNode.class);
    private String type = "virtualRemoteRepository";
    private RepoType repoPkgType;
    private RepoBaseDescriptor repoBaseDescriptor;
    private HttpServletRequest request;

    public VirtualRemoteRepositoryNode(RepoBaseDescriptor repo, String repoType, ArtifactoryRestRequest request) {
        super(InternalRepoPathFactory.create(repo.getKey(), ""));
        initRepositoryNode(repo, repoType);
        AuthorizationService authorizationService = ContextHelper.get().getAuthorizationService();
        this.repoBaseDescriptor = repo;
        populateActions(authorizationService);
        this.request = request.getServletRequest();
        populateTabs(authorizationService);
    }

    /**
     * initialize repository node
     *
     * @param repo     - repository descriptor
     * @param repoType - repo type
     */
    private void initRepositoryNode(RepoBaseDescriptor repo, String repoType) {
        setRepoType(repoType);
        RepoPath repoPath = InternalRepoPathFactory.create(repo.getKey(), "");
        super.setText(repoPath.getRepoKey());
        setLocal(false);
        setHasChild(true);
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

    @Override
    public void populateActions(AuthorizationService authService) {
        List<IAction> actions = new ArrayList<>();
        boolean admin = authService.isAdmin();
        addRefreshAction(actions);
        addVirtualZapCaches(actions, admin);
        addRecalculateIndexAction(actions, admin);
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

    /**
     * add recalculate index for remote/virtual repositories
     *
     * @param actions - actions list
     */
    private void addRecalculateIndexAction(List<IAction> actions, boolean isAdmin) {
        if (isAdmin) {
            if (RegExUtils.REMOTE_REPO_REINDEX_PATTERN.matcher(this.repoPkgType.name()).matches()
                    && getRepoType().equals("remote")) {
                actions.add(new BaseArtifact("RecalculateIndex"));
            } else {
                Matcher matcher = RegExUtils.VIRTUAL_REPO_REINDEX_PATTERN.matcher(this.repoPkgType.name());
                boolean foundMatch = matcher.matches();
                if (foundMatch && getRepoType().equals("virtual")) {
                    actions.add(new BaseArtifact("RecalculateIndex"));
                }
            }
        }
    }

    /**
     * add zap caches action to virtual
     *
     * @param actions - actions list
     * @param isAdmin - if true use is admin
     */
    private void addVirtualZapCaches(List<IAction> actions, boolean isAdmin) {
        if (isAdmin) {
            if (getRepoType().equals("virtual")) {
                actions.add(new BaseArtifact("ZapCaches"));
            }
        }
    }

    @Override
    public void populateTabs(AuthorizationService authService) {
        List<IArtifactInfo> tabs = new ArrayList<>();
        BaseArtifactInfo general = addGeneralArtifactInfo();
        tabs.add(general);
        super.setTabs(tabs);
    }

    /**
     * addd general tab for remote and virtual repo
     *
     * @return
     */
    private BaseArtifactInfo addGeneralArtifactInfo() {
        VirtualRemoteRepoGeneralArtifactInfo general = new VirtualRemoteRepoGeneralArtifactInfo("General");
        general.setPath(getPath());
        general.setRepoKey(getRepoKey());
        general.populateGeneralData(repoBaseDescriptor, request);
        return general;
    }


    @Override
    protected RepoPath fetchRepoPath() {
        return super.getRepoPath();
    }


    @Override
    public Collection<? extends RestModel> fetchItemTypeData(AuthorizationService authService, boolean isCompact,
            Properties props, ArtifactoryRestRequest request) {
        // get repository or folder children 1st depth
        return getRepoOrFolderChildren(authService, isCompact, request);
    }

    /**
     * get repository or folder children
     *
     * @param authService - authorization service
     * @param isCompact   - is compacted
     * @param request
     * @return
     */
    private Collection<? extends RestModel> getRepoOrFolderChildren(AuthorizationService authService,
            boolean isCompact, ArtifactoryRestRequest request) {
        Collection<? extends RestTreeNode> items = getChildren(authService, isCompact, request);
        List<RestModel> treeModel = new ArrayList<>();
        items.forEach(item -> {
            // update additional data
            ((INode) item).updateNodeData();
            treeModel.add(item);
        });
        return treeModel;
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
