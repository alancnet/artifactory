package org.artifactory.webapp.wicket.page.search.actionable;

import org.apache.wicket.markup.html.panel.Panel;
import org.artifactory.api.bintray.BintrayItemInfo;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.RepoAwareActionableItemBase;
import org.artifactory.webapp.actionable.action.DownloadAction;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.actionable.action.ShowInBintrayAction;
import org.artifactory.webapp.actionable.action.ShowInTreeAction;

import java.util.Set;

/**
 * Actionable remote item
 *
 * @author Gidi Shabat
 */
public class ActionableExternalItemSearchResult<T extends BintrayItemInfo> extends RepoAwareActionableItemBase {
    public ShowInBintrayAction showInBintray;
    public ShowInTreeAction showInThree;
    protected DownloadAction downloadAction;
    private RemoteRepoDescriptor jCenterDescriptor;
    private T searchResult;

    public ActionableExternalItemSearchResult(RemoteRepoDescriptor jCenterDescriptor, T searchResult,
            RepoPath repoPath) {
        super(repoPath);
        this.jCenterDescriptor = jCenterDescriptor;
        this.searchResult = searchResult;
        Set<ItemAction> actions = getActions();
        addActions(actions);
    }

    protected void addActions(Set<ItemAction> actions) {
        downloadAction = new DownloadAction();
        showInBintray = new ShowInBintrayAction(searchResult);
        showInThree = new ShowInTreeAction();
        actions.add(downloadAction);
        actions.add(showInBintray);
        actions.add(showInThree);
    }

    @Override
    public void filterActions(AuthorizationService authService) {
        downloadAction.setEnabled(jCenterDescriptor != null);
        showInThree.setEnabled(
                jCenterDescriptor != null && searchResult.getCached() && jCenterDescriptor.isStoreArtifactsLocally());
    }

    @Override
    public Panel newItemDetailsPanel(String id) {
        throw new UnsupportedOperationException("method not allowed on search result");
    }

    @Override
    public String getDisplayName() {
        //todo finish the code
        return "display name";
    }

    @Override
    public String getCssClass() {
        //todo finish the code
        return "CssClaa";
    }

    @SuppressWarnings("UnusedDeclaration")
    public T getSearchResult() {
        return searchResult;
    }
}