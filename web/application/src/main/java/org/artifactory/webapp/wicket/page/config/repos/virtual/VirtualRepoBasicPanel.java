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

package org.artifactory.webapp.wicket.page.config.repos.virtual;

import com.google.common.collect.Lists;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.common.wicket.component.dnd.select.DragDropSelection;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoResolver;
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.webapp.wicket.components.IconDragDropSelection;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.page.config.repos.CachingDescriptorHelper;
import org.artifactory.webapp.wicket.page.config.repos.RepoGeneralSettingsPanel;

import java.util.Iterator;
import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public class VirtualRepoBasicPanel extends Panel {

    private final CreateUpdateAction action;
    private final VirtualRepoDescriptor repoDescriptor;
    private final CachingDescriptorHelper cachingDescriptorHelper;

    public VirtualRepoBasicPanel(String id, CreateUpdateAction action, VirtualRepoDescriptor repoDescriptor,
            CachingDescriptorHelper cachingDescriptorHelper) {
        super(id);
        this.action = action;
        this.repoDescriptor = repoDescriptor;
        this.cachingDescriptorHelper = cachingDescriptorHelper;

        add(new RepoGeneralSettingsPanel("generalSettings"));

        // resolved repos
        final WebMarkupContainer resolvedRepo = new WebMarkupContainer("resolvedRepoWrapper");
        resolvedRepo.setOutputMarkupId(true);
        add(resolvedRepo);
        add(new HelpBubble("resolvedRepo.help", new ResourceModel("resolvedRepo.help")));

        resolvedRepo.add(new DataView<RealRepoDescriptor>("resolvedRepo", new ResolvedReposDataProvider()) {
            @Override
            protected void populateItem(Item<RealRepoDescriptor> item) {
                RepoDescriptor repo = item.getModelObject();
                item.add(new Label("key", repo.getKey()));
                WebMarkupContainer note = new WebMarkupContainer("note");
                item.add(note);

                if (!isLayoutCompatible(repo)) {
                    note.replaceWith(new Label("note", "!"));
                }
            }
        });

        // repositories
        List<RepoDescriptor> repos = getReposExcludingCurrent();
        DragDropSelection<RepoDescriptor> reposSelection =
                new IconDragDropSelection<RepoDescriptor>("repositories", repos) {
                    @Override
                    protected void onOrderChanged(AjaxRequestTarget target) {
                        super.onOrderChanged(target);
                        target.add(resolvedRepo);
                        ModalHandler.resizeCurrent();
                    }
                };
        add(reposSelection);
        add(new SchemaHelpBubble("repositories.help"));
    }

    private boolean isLayoutCompatible(RepoDescriptor resolved) {
        RepoLayout virtualRepoLayout = repoDescriptor.getRepoLayout();
        RepoLayout resolverRepoLayout = resolved.getRepoLayout();

        return (virtualRepoLayout == null) || (resolverRepoLayout == null) ||
                RepoLayoutUtils.layoutsAreCompatible(virtualRepoLayout, resolverRepoLayout);
    }

    private List<RepoDescriptor> getReposExcludingCurrent() {
        // get all the list of available repositories excluding the current virtual repo
        List<RepoDescriptor> repos = getAllRepos();
        if (action.equals(CreateUpdateAction.UPDATE)) {
            repos.remove(repoDescriptor);
        }
        return repos;
    }

    private List<RepoDescriptor> getAllRepos() {
        List<RepoDescriptor> result = Lists.newArrayList();
        MutableCentralConfigDescriptor mutableDescriptor = cachingDescriptorHelper.getModelMutableDescriptor();
        result.addAll(mutableDescriptor.getLocalRepositoriesMap().values());
        result.addAll(mutableDescriptor.getRemoteRepositoriesMap().values());
        result.addAll(mutableDescriptor.getVirtualRepositoriesMap().values());
        return result;
    }

    private class ResolvedReposDataProvider implements IDataProvider<RealRepoDescriptor> {
        private final VirtualRepoResolver resolver = new VirtualRepoResolver(repoDescriptor);

        @Override
        public Iterator<RealRepoDescriptor> iterator(int first, int count) {
            return resolver.getOrderedRepos().iterator();
        }

        @Override
        public int size() {
            resolver.update(repoDescriptor);
            return resolver.getOrderedRepos().size();
        }

        @Override
        public IModel<RealRepoDescriptor> model(RealRepoDescriptor object) {
            return new Model<>(object);
        }

        @Override
        public void detach() {

        }
    }
}
