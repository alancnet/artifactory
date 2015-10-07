package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import org.artifactory.addon.AddonType;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.watch.ArtifactWatchAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.fs.WatchersInfo;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.BaseArtifact;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.IAction;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.IArtifactInfo;
import org.artifactory.util.Pair;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@JsonIgnoreProperties("repoPath")
public abstract class BaseNode implements INode {

    public BaseNode() {
    }
    private List<IAction> actions = null;
    private List<INode> childrens = null;
    private List<IArtifactInfo> tabs = null;
    private boolean hasChild = false;
    private RepoPath repoPath;
    private String repoKey;
    private String path;
    private String text;
    private boolean local = true;
    String repoType;
    private String icon;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<IArtifactInfo> getTabs() {
        return tabs;
    }

    public List<IArtifactInfo> fetchTabs() {
        if (tabs == null) {
            tabs = new ArrayList<>();
        }
        return tabs;
    }

    public void setTabs(List<IArtifactInfo> tabs) {
        this.tabs = tabs;
    }

    public BaseNode(RepoPath repoPath) {
        this.repoPath = repoPath;
        this.repoKey = repoPath.getRepoKey();
        this.path = repoPath.getPath();
        this.repoPath = repoPath;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    public String getRepoKey() {
        return repoKey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setRepoPath(RepoPath repoPath) {
        this.repoPath = repoPath;
        if (repoPath != null) {
            this.repoKey = repoPath.getRepoKey();
            this.path = repoPath.getPath();
        }
    }

    public boolean isHasChild() {
        return hasChild;
    }

    public void setHasChild(boolean hasChild) {
        this.hasChild = hasChild;
    }

    public List<IAction> getActions() {
        return actions;
    }

    public void setActions(List<IAction> actions) {
        this.actions = actions;
    }

    public List<INode> getChildrens() {
        return childrens;
    }

    public void setChildrens(List<INode> childrens) {
        this.childrens = childrens;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BaseNode)) {
            return false;
        }
        BaseNode base = (BaseNode) o;
        return repoPath.equals(base.repoPath);
    }

    @Override
    public int hashCode() {
        return repoPath.hashCode();
    }

    /**
     * populate watch action
     *
     * @param authService - authorization service
     * @param actions     - list of action
     * @param canRead     - if true , can read
     */
    public void addWatchAction(AuthorizationService authService, List<IAction> actions, boolean canRead) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        ArtifactWatchAddon watchAddon = addonsManager.addonByType(
                ArtifactWatchAddon.class);
        if (addonsManager.isAddonSupported(
                AddonType.WATCH) && canRead && !authService.isAnonymous() && !isThisBranchHasWatchAlready(authService,
                watchAddon)) {
            if (isUserWatchingRepoPath(authService, getRepoPath(), watchAddon)) {
                actions.add(new BaseArtifact("Unwatch"));
            } else {
                actions.add(new BaseArtifact("Watch"));
            }
        }
    }

    /**
     * check if anyone is watching this path branch already
     *
     * @param authService - authorization service
     * @param watchAddon  - watch addon
     * @return if true - this path branch has watch already
     */
    private boolean isThisBranchHasWatchAlready(AuthorizationService authService,
            ArtifactWatchAddon watchAddon) {
        Pair<RepoPath, WatchersInfo> nearestWatch = watchAddon.getNearestWatchDefinition(
                getRepoPath(), authService.currentUsername());
        return nearestWatch != null && !(nearestWatch.getFirst().getPath().equals(getRepoPath().getPath()));
    }

    public abstract void populateActions(AuthorizationService authService);

    @Override
    public void populateTabs(AuthorizationService authService) {
        if (isLocal()) {
            List<IArtifactInfo> tabs = new ArrayList<>();
            boolean canAdminRepoPath = authService.canManage(getRepoPath());
            addGeneralTab(tabs);
            addEffectivePermissionTab(tabs, canAdminRepoPath);
            addPropertiesTab(tabs);
            addWatchTab(tabs, canAdminRepoPath);
            this.tabs = tabs;
        }
    }

    protected abstract RepoPath fetchRepoPath();

    /**
     * add properties tab
     *
     * @param tabs - ttabs list
     */
    protected void addPropertiesTab(List<IArtifactInfo> tabs) {
        tabs.add(new BaseArtifactInfo("Properties"));
    }

    /**
     * add general tab
     *
     * @param tabs - tabs list
     */
    protected void addGeneralTab(List<IArtifactInfo> tabs) {
        tabs.add(new BaseArtifactInfo("General"));
    }

    /**
     * populate watch tab
     *
     * @param tabs             - tabs list
     * @param canAdminRepoPath - if true , has admin permission for this repo
     */
    protected void addWatchTab(List<IArtifactInfo> tabs, boolean canAdminRepoPath) {
        if (canAdminRepoPath) {
            tabs.add(new BaseArtifactInfo("Watch"));
        }
    }

    /**
     * populate effective permission tab
     *
     * @param tabs             - tabs list
     * @param canAdminRepoPath - if true , has admin permission for this repo
     */
    protected void addEffectivePermissionTab(List<IArtifactInfo> tabs, boolean canAdminRepoPath) {
        if (canAdminRepoPath) {
            tabs.add(new BaseArtifactInfo("EffectivePermission"));
        }
    }

    @Override
    public void updateNodeData() {
        if (isLocal()) {
            hasChild = getRepoService().hasChildren(repoPath);
        }
    }

    /**
     * retrieve local or cached repo descriptor
     *
     * @param repoPath - repo path
     * @return local repo descriptor
     */
    public LocalRepoDescriptor localOrCachedRepoDescriptor(RepoPath repoPath) {
        String repoKey = repoPath.getRepoKey();
        return getRepoService().localOrCachedRepoDescriptorByKey(repoKey);
    }

    /**
     * check if user watching repo path
     *
     * @param authService - authorization service
     * @param repoPath    - repo path
     * @param artifactWatchAddon watch addon
     * @return if true - user is watching repo service
     */
    protected boolean isUserWatchingRepoPath(AuthorizationService authService, RepoPath repoPath,
            ArtifactWatchAddon artifactWatchAddon) {
        return artifactWatchAddon.isUserWatchingRepo(repoPath, authService.currentUsername());
    }


    /**
     * add delete action
     *
     * @param actions   - actions list
     * @param canDelete - can user delete
     */
    protected void addDeleteAction(List<IAction> actions, boolean canDelete) {
        if (canDelete) {
            actions.add(new BaseArtifact("Delete"));
        }
    }

    /**
     * populate zap actions
     *
     * @param actions  - list of actions
     * @param repoPath - repo path
     * @param canAdmin -if true , has admin permission
     */
    protected void addZapAction(List<IAction> actions, RepoPath repoPath, boolean canAdmin) {
        if (canAdmin && localOrCachedRepoDescriptor(repoPath).isCache()) {
            actions.add(new BaseArtifact("Zap"));
        }
    }

    /**
     * populate Move action
     *
     * @param authService - authorization serivce
     * @param actions     - list of actions
     * @param repoPath    - repo path
     * @param canDelete   - if true , can delete
     */
    protected void addMoveAction(AuthorizationService authService, List<IAction> actions, RepoPath repoPath,
            boolean canDelete) {
        if (canDelete && !NamingUtils.isSystem(repoPath.getPath()) && authService.canDeployToLocalRepository()) {
            actions.add(new BaseArtifact("Move"));
        }
    }

    /**
     * populate copy action
     *
     * @param authService - authorization service
     * @param actions     - list of action
     * @param repoPath    - repo path
     * @return can read permission
     */
    protected boolean addCopyAction(AuthorizationService authService, List<IAction> actions, RepoPath repoPath) {
        boolean canRead = authService.canRead(repoPath);
        if (canRead && !NamingUtils.isSystem(repoPath.getPath()) && authService.canDeployToLocalRepository()) {
            actions.add(new BaseArtifact("Copy"));
        }
        return canRead;
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
