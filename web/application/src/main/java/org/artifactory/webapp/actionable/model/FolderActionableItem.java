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

package org.artifactory.webapp.actionable.model;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.DockerWebAddon;
import org.artifactory.addon.wicket.WatchAddon;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.CannonicalEnabledActionableFolder;
import org.artifactory.webapp.actionable.RefreshableActionableItem;
import org.artifactory.webapp.actionable.action.CopyAction;
import org.artifactory.webapp.actionable.action.DeleteVersionsAction;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.actionable.action.MoveAction;
import org.artifactory.webapp.actionable.action.RefreshNodeAction;
import org.artifactory.webapp.actionable.action.ZapAction;
import org.artifactory.webapp.wicket.util.ItemCssClass;

import java.util.List;
import java.util.Set;

/**
 * @author yoavl
 */
public class FolderActionableItem extends CachedItemActionableItem
        implements HierarchicActionableItem, CannonicalEnabledActionableFolder, RefreshableActionableItem {

    /**
     * The folder info of the last element of the compacted folder or the current folder if not compacted.
     */
    private FolderInfo folderInfo;
    private String displayName;
    private ItemAction deleteAction;
    private MoveAction moveAction;
    private CopyAction copyAction;
    private ItemAction zapAction;
    private DeleteVersionsAction delVersions;
    private boolean compactAllowed;
    private ItemAction watchAction;
    private String cssClass;

    public FolderActionableItem(org.artifactory.fs.FolderInfo folderInfo, boolean compactAllowed) {
        super(folderInfo.getRepoPath());
        this.folderInfo = folderInfo;
        this.compactAllowed = compactAllowed;
        this.displayName = this.folderInfo.getName();

        if (isCompactAllowed()) {
            compact();
        }

        addActions();

        AddonsManager addonsManager = getAddonsProvider();
        DockerWebAddon dockerWebAddon = addonsManager.addonByType(DockerWebAddon.class);
        cssClass = dockerWebAddon.getFolderCssClass(getRepoPath(), getRepo());
        if (StringUtils.isBlank(cssClass)) {
            cssClass = isCompacted() ? ItemCssClass.folderCompact.getCssClass() : ItemCssClass.folder.getCssClass();
        }
    }

    private void compact() {
        List<FolderInfo> compactedFolders = getRepoService().getWithEmptyChildren(this.folderInfo);

        //Change the icon if compacted
        int size = compactedFolders.size();
        if (size > 1) {
            displayName = calcCompactedDisplayName(compactedFolders);
            folderInfo = compactedFolders.get(size - 1);
        }
    }

    private String calcCompactedDisplayName(List<FolderInfo> folderList) {
        StringBuilder name = new StringBuilder();
        for (FolderInfo folder : folderList) {
            name.append('/').append(folder.getName());
        }
        return name.substring(1);
    }

    @Override
    public boolean isCompactAllowed() {
        return compactAllowed;
    }

    @Override
    public void setCompactAllowed(boolean compactAllowed) {
        this.compactAllowed = compactAllowed;
    }

    @Override
    public RepoPath getCanonicalPath() {
        return getFolderInfo().getRepoPath();
    }

    private void addActions() {
        Set<ItemAction> actions = getActions();
        actions.add(new RefreshNodeAction());
        moveAction = new MoveAction();
        actions.add(moveAction);
        copyAction = new CopyAction();
        actions.add(copyAction);
        zapAction = new ZapAction();
        actions.add(zapAction);
        delVersions = new DeleteVersionsAction();
        actions.add(delVersions);

        AddonsManager addonsManager = getAddonsProvider();
        DockerWebAddon dockerWebAddon = addonsManager.addonByType(DockerWebAddon.class);
        deleteAction = dockerWebAddon.getDeleteAction(folderInfo);
        actions.add(deleteAction);

        WatchAddon watchAddon = addonsManager.addonByType(WatchAddon.class);
        watchAction = watchAddon.getWatchAction(folderInfo.getRepoPath());
        actions.add(watchAction);
    }

    /**
     * The folder info of the last element of the compacted folder or the current folder if not compacted.
     */
    public FolderInfo getFolderInfo() {
        return folderInfo;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getCssClass() {
        return cssClass;
    }

    @Override
    public void refresh() {
        children = null;    // set the children to null will force reload
    }

    @Override
    public List<ActionableItem> getChildren(AuthorizationService authService) {
        boolean childrenCacheUpToDate = childrenCacheUpToDate();
        if (!childrenCacheUpToDate) {
            RepositoryService repoService = getRepoService();
            List<ItemInfo> items = repoService.getChildren(getCanonicalPath());

            children = Lists.newArrayListWithExpectedSize(items.size());
            for (ItemInfo pathItem : items) {
                //Check if we should return the child
                if (!repoService.isRepoPathVisible(pathItem.getRepoPath())) {
                    continue;
                }

                String name = pathItem.getName();
                //Skip checksum files
                if (NamingUtils.isChecksum(name)) {
                    continue;
                }
                //No need to check for null as children is set before the iteration
                //noinspection ConstantConditions
                children.add(getChildItem(pathItem, pathItem.getRelPath(), compactAllowed));
            }
        }
        return children;
    }

    @Override
    public boolean hasChildren(AuthorizationService authService) {
        RepoPath repoPath = getCanonicalPath();
        return getRepoService().hasChildren(repoPath);
    }

    @Override
    public void filterActions(AuthorizationService authService) {
        RepoPath repoPath = getCanonicalPath();
        boolean canDelete = authService.canDelete(repoPath);
        if (!canDelete) {
            deleteAction.setEnabled(false);
        }
        if (!canDelete) {
            zapAction.setEnabled(false);
        } else if (!getRepo().isCache()) {
            zapAction.setEnabled(false);
        }

        // only admin can cleanup by version
        if (!authService.isAdmin()) {
            delVersions.setEnabled(false);
        }

        if (!canDelete || NamingUtils.isSystem(repoPath.getPath()) || !authService.canDeployToLocalRepository()) {
            moveAction.setEnabled(false);
        }

        boolean canRead = authService.canRead(repoPath);
        if (!canRead || NamingUtils.isSystem(repoPath.getPath()) || !authService.canDeployToLocalRepository()) {
            copyAction.setEnabled(false);
        }

        if (!canRead || authService.isAnonymous()) {
            watchAction.setEnabled(false);
        }
    }

    private boolean isCompacted() {
        return !getRepoPath().equals(folderInfo.getRepoPath());
    }

    public boolean hasStatsInfo() {
        return false;
    }
}
