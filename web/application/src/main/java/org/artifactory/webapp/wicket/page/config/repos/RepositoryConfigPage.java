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

package org.artifactory.webapp.wicket.page.config.repos;

import com.google.common.collect.Maps;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.ajax.CancelDefaultDecorator;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.confirm.AjaxConfirm;
import org.artifactory.common.wicket.component.confirm.ConfirmDialog;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.modal.links.ModalShowLink;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.panel.sortedlist.OrderedListPanel;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.util.RepoPathUtils;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.page.config.SchemaHelpModel;
import org.artifactory.webapp.wicket.page.config.repos.local.LocalRepoPanel;
import org.artifactory.webapp.wicket.page.config.repos.remote.HttpRepoPanel;
import org.artifactory.webapp.wicket.page.config.repos.remote.importer.RemoteRepoImportPanel;
import org.artifactory.webapp.wicket.page.config.repos.virtual.VirtualRepoPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.artifactory.common.wicket.component.CreateUpdateAction.CREATE;
import static org.artifactory.common.wicket.component.CreateUpdateAction.UPDATE;

/**
 * Repositories configuration page.
 *
 * @author Yossi Shaul
 */
@SuppressWarnings({"OverlyComplexAnonymousInnerClass"})
@AuthorizeInstantiation(AuthorizationService.ROLE_ADMIN)
public class RepositoryConfigPage extends AuthenticatedPage {
    private static final Logger log = LoggerFactory.getLogger(RepositoryConfigPage.class);

    @SpringBean
    private CentralConfigService centralConfigService;

    @SpringBean
    private RepositoryService repositoryService;

    @SpringBean
    private AddonsManager addons;

    private MutableCentralConfigDescriptor mutableDescriptor;

    private CachingDescriptorHelper cachingDescriptorHelper;

    public static final String REPO_ID = "repoKey";

    public RepositoryConfigPage(PageParameters parameters) {
        mutableDescriptor = refreshDescriptor();
        cachingDescriptorHelper = new CachingDescriptorHelper(mutableDescriptor);
        RepoBaseDescriptor descriptor = null;
        String repoKey = parameters.get(REPO_ID).toString();
        if (repoKey != null) {
            descriptor = getDescriptorFromRepoKey(repoKey);
        }
        addLocalReposList(descriptor);
        addRemoteReposList(descriptor);
        addVirtualReposList(descriptor);
    }

    private RepoBaseDescriptor getDescriptorFromRepoKey(String repoKey) {
        RepoBaseDescriptor descriptor = mutableDescriptor.getLocalRepositoriesMap().get(repoKey);
        if (descriptor == null) {
            descriptor = mutableDescriptor.getRemoteRepositoriesMap().get(repoKey);
            if (descriptor == null) {
                descriptor = mutableDescriptor.getVirtualRepositoriesMap().get(repoKey);
            }
        }
        return descriptor;
    }


    protected MutableCentralConfigDescriptor refreshDescriptor() {
        return centralConfigService.getMutableDescriptor();
    }

    @Override
    public String getPageName() {
        return "Configure Repositories";
    }

    private void addLocalReposList(final RepoBaseDescriptor descriptor) {
        final IModel<List<LocalRepoDescriptor>> repoListModel = new RepoListModel<LocalRepoDescriptor>() {
            @Override
            protected Collection<LocalRepoDescriptor> getRepos() {
                return mutableDescriptor.getLocalRepositoriesMap().values();
            }

            @Override
            protected void reOrderReposList(List<LocalRepoDescriptor> repos) {
                Map<String, LocalRepoDescriptor> currentLocalReposMap =
                        mutableDescriptor.getLocalRepositoriesMap();
                assertLegalReorderedList(repos, currentLocalReposMap.values());

                Map<String, LocalRepoDescriptor> localReposMap = Maps.newLinkedHashMap();
                for (LocalRepoDescriptor localRepo : repos) {
                    localReposMap.put(localRepo.getKey(), localRepo);
                }
                mutableDescriptor.setLocalRepositoriesMap(localReposMap);
            }
        };

        final RepoListPanel<LocalRepoDescriptor> panel = new RepoListPanel<LocalRepoDescriptor>("localRepos",
                repoListModel) {
            @Override
            protected void saveItems(AjaxRequestTarget target) {
                try {
                    cachingDescriptorHelper.syncAndSaveLocalRepositories();
                    info("Local repositories order was successfully saved.");
                } catch (Exception e) {
                    error("Could not save local repositories order: " + e.getMessage());
                }
                AjaxUtils.refreshFeedback(target);
            }

            @Override
            protected void resetListOrder(AjaxRequestTarget target) {
                MutableCentralConfigDescriptor configDescriptor = cachingDescriptorHelper.getSavedMutableDescriptor();
                List<LocalRepoDescriptor> repoDescriptorList =
                        new ArrayList<>(configDescriptor.getLocalRepositoriesMap().values());
                repoListModel.setObject(repoDescriptorList);
                ((RepositoryConfigPage) getPage()).refresh(target);
            }

            @Override
            protected String getItemDisplayValue(LocalRepoDescriptor itemObject) {
                return itemObject.getKey();
            }

            @Override
            protected BaseModalPanel newCreateItemPanel() {
                LocalRepoDescriptor repoDescriptor = new LocalRepoDescriptor();
                repoDescriptor.setRepoLayout(RepoLayoutUtils.MAVEN_2_DEFAULT);
                LicensesAddon licensesAddon = addons.addonByType(LicensesAddon.class);
                licensesAddon.addPropertySetToRepository(repoDescriptor);
                return new LocalRepoPanel(CREATE, repoDescriptor, cachingDescriptorHelper);
            }

            @Override
            protected BaseModalPanel newUpdateItemPanel(LocalRepoDescriptor itemObject) {
                return new LocalRepoPanel(UPDATE, itemObject, cachingDescriptorHelper);
            }


            @Override
            protected Component newToolbar(String id) {
                return new SchemaHelpBubble(id, getHelpModel("localRepositoriesMap"));
            }

            @Override
            protected List<AbstractLink> getItemActions(LocalRepoDescriptor itemObject, String linkId) {
                List<AbstractLink> links = super.getItemActions(itemObject, linkId);
                if (mutableDescriptor.getLocalRepositoriesMap().size() == 1) {
                    // don't allow deletion of the last local repository
                    removeDeleteLink(links);
                }
                return links;
            }

            private void removeDeleteLink(List<AbstractLink> links) {
                Iterator<AbstractLink> linkIterator = links.iterator();
                while (linkIterator.hasNext()) {
                    AbstractLink link = linkIterator.next();
                    if (link instanceof DeleteRepoLink) {
                        linkIterator.remove();
                    }
                }
            }
        };
        add(panel);
        if (descriptor != null && descriptor instanceof LocalRepoDescriptor) {
            panel.addAjaxTimer((LocalRepoDescriptor) descriptor);
        }
    }

    private void addRemoteReposList(final RepoBaseDescriptor descriptor) {
        final IModel<List<RemoteRepoDescriptor>> repoListModel = new RepoListModel<RemoteRepoDescriptor>() {
            @Override
            protected Collection<RemoteRepoDescriptor> getRepos() {
                return mutableDescriptor.getRemoteRepositoriesMap().values();
            }

            @Override
            protected void reOrderReposList(List<RemoteRepoDescriptor> repos) {
                Map<String, RemoteRepoDescriptor> currentReposMap =
                        mutableDescriptor.getRemoteRepositoriesMap();
                assertLegalReorderedList(repos, currentReposMap.values());

                Map<String, RemoteRepoDescriptor> remoteReposMap = Maps.newLinkedHashMap();
                for (RemoteRepoDescriptor remoteRepo : repos) {
                    remoteReposMap.put(remoteRepo.getKey(), remoteRepo);
                }
                mutableDescriptor.setRemoteRepositoriesMap(remoteReposMap);
            }
        };

        final RepoListPanel<RemoteRepoDescriptor> panel = new RepoListPanel<RemoteRepoDescriptor>(
                "remoteRepos", repoListModel) {
            @Override
            protected void saveItems(AjaxRequestTarget target) {
                try {
                    cachingDescriptorHelper.syncAndSaveRemoteRepositories();
                    info("Remote repositories order was successfully saved.");
                } catch (Exception e) {
                    error("Could not save remote repositories order: " + e.getMessage());
                }
                AjaxUtils.refreshFeedback(target);

            }

            @Override
            protected void resetListOrder(AjaxRequestTarget target) {
                MutableCentralConfigDescriptor configDescriptor = cachingDescriptorHelper.getSavedMutableDescriptor();
                List<RemoteRepoDescriptor> remoteRepoDescriptors =
                        new ArrayList<>(configDescriptor.getRemoteRepositoriesMap().values());
                repoListModel.setObject(remoteRepoDescriptors);
                ((RepositoryConfigPage) getPage()).refresh(target);
            }

            @Override
            protected String getItemDisplayValue(RemoteRepoDescriptor itemObject) {
                return itemObject.getKey();
            }

            @Override
            protected BaseModalPanel newCreateItemPanel() {
                HttpRepoDescriptor repoDescriptor = new HttpRepoDescriptor();
                repoDescriptor.setRepoLayout(RepoLayoutUtils.MAVEN_2_DEFAULT);
                LicensesAddon licensesAddon = addons.addonByType(LicensesAddon.class);
                licensesAddon.addPropertySetToRepository(repoDescriptor);
                return new HttpRepoPanel(CREATE, repoDescriptor, cachingDescriptorHelper);
            }

            @Override
            protected HttpRepoPanel newUpdateItemPanel(RemoteRepoDescriptor itemObject) {
                return new HttpRepoPanel(UPDATE, (HttpRepoDescriptor) itemObject, cachingDescriptorHelper);
            }

            @Override
            protected Component newToolbar(String id) {
                return new SchemaHelpBubble(id, getHelpModel("remoteRepositoriesMap"));
            }

            @Override
            protected Component getImportLink(String id) {
                return new ModalShowLink(id, "Import") {
                    @Override
                    protected BaseModalPanel getModelPanel() {
                        return new RemoteRepoImportPanel(cachingDescriptorHelper);
                    }
                };
            }
        };
        add(panel);
        if (descriptor != null && descriptor instanceof RemoteRepoDescriptor) {
            panel.addAjaxTimer((RemoteRepoDescriptor) descriptor);
        }
    }

    private void addVirtualReposList(final RepoBaseDescriptor descriptor) {
        final IModel<List<VirtualRepoDescriptor>> repoListModel = new RepoListModel<VirtualRepoDescriptor>() {
            @Override
            protected Collection<VirtualRepoDescriptor> getRepos() {
                return mutableDescriptor.getVirtualRepositoriesMap().values();
            }

            @Override
            protected void reOrderReposList(List<VirtualRepoDescriptor> repos) {
                Map<String, VirtualRepoDescriptor> currentReposMap =
                        mutableDescriptor.getVirtualRepositoriesMap();
                assertLegalReorderedList(repos, currentReposMap.values());

                Map<String, VirtualRepoDescriptor> virtualReposMap = Maps.newLinkedHashMap();
                for (VirtualRepoDescriptor virtualRepo : repos) {
                    virtualReposMap.put(virtualRepo.getKey(), virtualRepo);
                }
                mutableDescriptor.setVirtualRepositoriesMap(virtualReposMap);
            }
        };

        final RepoListPanel<VirtualRepoDescriptor> panel = new RepoListPanel<VirtualRepoDescriptor>(
                "virtualRepos", repoListModel) {
            @Override
            protected void saveItems(AjaxRequestTarget target) {
                try {
                    cachingDescriptorHelper.syncAndSaveVirtualRepositories(false, false);
                    info("Virtual repositories order was successfully saved.");
                } catch (Exception e) {
                    error("Could not save virtual repositories order: " + e.getMessage());
                }
                AjaxUtils.refreshFeedback(target);
            }


            @Override
            protected void resetListOrder(AjaxRequestTarget target) {
                MutableCentralConfigDescriptor configDescriptor = cachingDescriptorHelper.getSavedMutableDescriptor();
                ArrayList<VirtualRepoDescriptor> virtualRepoDescriptors =
                        new ArrayList<>(configDescriptor.getVirtualRepositoriesMap().values());
                repoListModel.setObject(virtualRepoDescriptors);
                ((RepositoryConfigPage) getPage()).refresh(target);
            }

            @Override
            protected String getItemDisplayValue(VirtualRepoDescriptor itemObject) {
                return itemObject.getKey();
            }

            @Override
            protected BaseModalPanel newCreateItemPanel() {
                VirtualRepoDescriptor repoDescriptor = new VirtualRepoDescriptor();
                return new VirtualRepoPanel(CREATE, repoDescriptor, cachingDescriptorHelper);
            }

            @Override
            protected BaseModalPanel newUpdateItemPanel(VirtualRepoDescriptor itemObject) {
                // Update the model being updated with the latest state.
                itemObject = refreshDescriptor().getVirtualRepositoriesMap().get(itemObject.getKey());
                return new VirtualRepoPanel(UPDATE, itemObject, cachingDescriptorHelper);
            }

            @Override
            protected Component newToolbar(String id) {
                return new SchemaHelpBubble(id, getHelpModel("virtualRepositoriesMap"));
            }
        };
        add(panel);
        if (descriptor != null && descriptor instanceof VirtualRepoDescriptor) {
            panel.addAjaxTimer((VirtualRepoDescriptor) descriptor);
        }
    }

    private SchemaHelpModel getHelpModel(String property) {
        return new SchemaHelpModel(mutableDescriptor, property);
    }

    public void refresh(final AjaxRequestTarget target) {
        visitChildren(RepoListPanel.class, new IVisitor<RepoListPanel, Void>() {
            @Override
            public void component(RepoListPanel repoListPanel, IVisit<Void> visit) {
                repoListPanel.refresh(target);
            }
        });
    }

    private class DeleteRepoLink extends TitledAjaxLink {
        private final RepoDescriptor repoDescriptor;

        private DeleteRepoLink(String linkId, RepoDescriptor repoDescriptor) {
            super(linkId, "Delete");
            add(new CssClass("icon-link DeleteAction"));
            this.repoDescriptor = repoDescriptor;
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            AjaxConfirm.get().confirm(new ConfirmDialog() {
                @Override
                public String getMessage() {
                    return getDeleteConfirmMessage();
                }

                @Override
                public void onConfirm(boolean approved, AjaxRequestTarget target) {
                    if (approved) {
                        deleteRepo(target);
                    }
                }
            });
        }

        private void deleteRepo(AjaxRequestTarget target) {
            String repoKey = repoDescriptor.getKey();
            cachingDescriptorHelper.removeRepositoryAndSave(repoKey);
            ((RepositoryConfigPage) getPage()).refresh(target);
        }

        private String getDeleteConfirmMessage() {
            String key = repoDescriptor.getKey();
            long count = 0;
            boolean hasCacheItems = false;
            if (repoDescriptor instanceof RemoteRepoDescriptor) {
                RepoDescriptor cacheRepoDescriptor = findRemoteCacheDescriptor();
                if (cacheRepoDescriptor != null) {
                    count = repositoryService.getArtifactCount(
                            RepoPathUtils.repoRootPath(cacheRepoDescriptor.getKey()));
                    hasCacheItems = true;
                }
            } else {
                count = repositoryService.getArtifactCount(RepoPathUtils.repoRootPath(key));
            }
            //if remote repo has no cache
            StringBuilder builder = new StringBuilder("Are you sure you wish to delete the repository '").append(
                    key).append("'?\n");
            if (!(repoDescriptor instanceof VirtualRepoDescriptor)) {
                builder.append("The repository ").append(key).append(" contains ").append(count).append(
                        " artifacts");
                builder.append(hasCacheItems ? " in cache repo" : "");
                builder.append(" which will be permanently deleted!");
            }
            return builder.toString();
        }

        private LocalCacheRepoDescriptor findRemoteCacheDescriptor() {
            List<LocalCacheRepoDescriptor> cacheRepoDescriptorList = repositoryService.getCachedRepoDescriptors();
            for (LocalCacheRepoDescriptor descriptor : cacheRepoDescriptorList) {
                RemoteRepoDescriptor remoteRepoDescriptor = descriptor.getRemoteRepo();
                if (remoteRepoDescriptor.equals(repoDescriptor)) {
                    return descriptor;
                }
            }
            return null;
        }
    }


    private abstract static class EditLink extends ModalShowLink {
        private EditLink(String linkId) {
            super(linkId, "Edit");
            add(new CssClass("icon-link UpdateAction"));
        }
    }

    private abstract class RepoListPanel<T extends RepoDescriptor> extends OrderedListPanel<T> {
        protected RepoListPanel(String id, IModel<List<T>> listModel) {
            super(id, listModel);
        }

        protected abstract BaseModalPanel newUpdateItemPanel(T itemObject);

        protected void addAjaxTimer(final T itemObject) {
            add(new AbstractAjaxTimerBehavior(Duration.milliseconds(1)) {
                @Override
                protected void onTimer(AjaxRequestTarget target) {
                    stop(); // don't fire again
                    ModalHandler modalHandler = ModalHandler.getInstanceFor(RepoListPanel.this);
                    modalHandler.setModalPanel(newUpdateItemPanel(itemObject));
                    modalHandler.show(target);
                }
            });
        }

        @Override
        protected void populateItem(final ListItem<T> item) {
            super.populateItem(item);
            item.add(new AjaxEventBehavior("ondblclick") {
                @SuppressWarnings({"unchecked"})
                @Override
                protected void onEvent(AjaxRequestTarget target) {
                    ModalHandler modalHandler = ModalHandler.getInstanceFor(RepoListPanel.this);
                    T itemObject = (T) item.getDefaultModelObject();
                    modalHandler.setModalPanel(newUpdateItemPanel(itemObject));
                    modalHandler.show(target);
                }

                @Override
                protected IAjaxCallDecorator getAjaxCallDecorator() {
                    return new CancelDefaultDecorator();
                }
            });
        }

        @Override
        protected List<AbstractLink> getItemActions(final T itemObject, String linkId) {
            List<AbstractLink> links = new ArrayList<>();
            links.add(new EditLink(linkId) {
                @Override
                protected BaseModalPanel getModelPanel() {
                    return newUpdateItemPanel(itemObject);
                }
            });
            links.add(new DeleteRepoLink(linkId, itemObject));
            return links;
        }

        @Override
        protected void onOrderChanged(AjaxRequestTarget target) {
            super.onOrderChanged(target);
            ((RepositoryConfigPage) getPage()).refresh(target);
        }

        @Override
        public String getTitle() {
            return getString(getId() + ".title", getDefaultModel());
        }
    }

    private abstract class RepoListModel<T extends RepoDescriptor> implements IModel<List<T>> {
        @Override
        public List<T> getObject() {
            return new ArrayList<>(getRepos());
        }

        @Override
        public void setObject(List<T> object) {
            reOrderReposList(object);
        }

        @Override
        public void detach() {
        }

        protected abstract Collection<T> getRepos();

        protected abstract void reOrderReposList(List<T> repos);

        protected void assertLegalReorderedList(List<? extends RepoDescriptor> newList,
                Collection<? extends RepoDescriptor> original) {
            log.debug("Original ordered list: {}", original);
            log.debug("New ordered list: {}", newList);
            // make sure that the new reordered list contains the same repositories count and the
            // same repository keys
            if (newList.size() != original.size()) {
                throw new IllegalArgumentException(
                        "Invalid reordered repositories list: size doesn't match. " +
                                "Expected " + original.size() +
                                " but received " + newList.size());
            }
            if (!original.containsAll(newList)) {
                throw new IllegalArgumentException("Invalid reordered repositories list: " +
                        "the new list contains repository not in the original list");
            }
        }
    }
}
