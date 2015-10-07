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

package org.artifactory.webapp.wicket.page.config.layout;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.border.fieldset.FieldSetBorder;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.webapp.wicket.page.config.repos.RepositoryConfigPage;
import org.artifactory.webapp.wicket.util.ItemCssClass;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Panel that shows the association of a certain {@link RepoLayout} to all repositories that it is currently associated
 * with
 *
 * @author Tomer Cohen
 */
public class RepositoriesListPanel extends TitledPanel {

    @SpringBean
    private CentralConfigService centralConfigService;

    @SpringBean
    private RepositoryService repositoryService;

    public RepositoriesListPanel(String id, RepoLayout layout) {
        super(id);
        add(new CssClass("horizontal-list"));
        addRepoList(layout);
    }

    private void addRepoList(RepoLayout layout) {
        List<RepoDescriptor> descriptors = getAllDescriptors();
        List<RepoDescriptor> filteredDescriptors = filterDescriptorsWithCurrentLayout(descriptors, layout);
        FieldSetBorder localFiledSetBorder = new FieldSetBorder("localFileSet") {
            @Override
            public String getTitle() {
                return "Local Repositories";
            }
        };
        List<RepoDescriptor> localRepoDescriptors = filterLocalRepos(filteredDescriptors);
        localFiledSetBorder.setVisible(!localRepoDescriptors.isEmpty());
        localFiledSetBorder.add(new ListView<RepoDescriptor>("localRepos", localRepoDescriptors) {
            @Override
            protected void populateItem(ListItem<RepoDescriptor> item) {
                final RepoDescriptor descriptor = item.getModelObject();
                Component link = new Label("local-link", descriptor.getKey());
                link.add(new CssClass("item-link"));
                item.add(link);
                item.add(new AjaxEventBehavior("onclick") {
                    @Override
                    protected void onEvent(AjaxRequestTarget target) {
                        PageParameters parameters = new PageParameters();
                        parameters.set(RepositoryConfigPage.REPO_ID, descriptor.getKey());
                        setResponsePage(RepositoryConfigPage.class, parameters);
                    }
                });
                link.add(new CssClass(getCssClassAccordingToRepo(descriptor)));
                item.add(link);
            }

            private String getCssClassAccordingToRepo(RepoDescriptor descriptor) {
                return ItemCssClass.getRepoCssClass(descriptor);
            }
        });
        add(localFiledSetBorder);
        FieldSetBorder remoteFileSetBorder = new FieldSetBorder("remoteFileSet") {
            @Override
            public String getTitle() {
                return "Remote Repositories";
            }
        };
        List<RepoDescriptor> remoteRepoDescriptors = filterRemoteRepos(filteredDescriptors);
        remoteFileSetBorder.setVisible(!remoteRepoDescriptors.isEmpty());
        add(remoteFileSetBorder);
        remoteFileSetBorder.add(new ListView<RepoDescriptor>("remoteRepos", remoteRepoDescriptors) {
            @Override
            protected void populateItem(ListItem<RepoDescriptor> item) {
                final RepoDescriptor descriptor = item.getModelObject();
                Component link = new Label("remote-link", descriptor.getKey());
                link.add(new CssClass("item-link"));
                item.add(link);
                item.add(new AjaxEventBehavior("onclick") {
                    @Override
                    protected void onEvent(AjaxRequestTarget target) {
                        PageParameters parameters = new PageParameters();
                        parameters.set(RepositoryConfigPage.REPO_ID, descriptor.getKey());
                        setResponsePage(RepositoryConfigPage.class, parameters);
                    }
                });
                link.add(new CssClass(getCssClassAccordingToRepo(descriptor)));
                item.add(link);
            }

            private String getCssClassAccordingToRepo(RepoDescriptor descriptor) {
                return ItemCssClass.getRepoCssClass(descriptor);
            }
        });
        FieldSetBorder virtualFieldSetBorder = new FieldSetBorder("virtualFieldSetBorder") {
            @Override
            public String getTitle() {
                return "Virtual Repositories";
            }
        };
        List<RepoDescriptor> virtualRepoDescriptors = filterVirtualRepos(filteredDescriptors);
        virtualFieldSetBorder.setVisible(!virtualRepoDescriptors.isEmpty());
        add(virtualFieldSetBorder);
        virtualFieldSetBorder.add(new ListView<RepoDescriptor>("virtualRepos", virtualRepoDescriptors) {
            @Override
            protected void populateItem(ListItem<RepoDescriptor> item) {
                final RepoDescriptor descriptor = item.getModelObject();
                Component link = new Label("virtual-link", descriptor.getKey());
                link.add(new CssClass("item-link"));
                item.add(link);
                item.add(new AjaxEventBehavior("onclick") {
                    @Override
                    protected void onEvent(AjaxRequestTarget target) {
                        PageParameters parameters = new PageParameters();
                        parameters.set(RepositoryConfigPage.REPO_ID, descriptor.getKey());
                        setResponsePage(RepositoryConfigPage.class, parameters);
                    }
                });
                link.add(new CssClass(getCssClassAccordingToRepo(descriptor)));
                item.add(link);
            }

            private String getCssClassAccordingToRepo(RepoDescriptor descriptor) {
                return ItemCssClass.getRepoCssClass(descriptor);
            }
        });

        setVisible(!virtualRepoDescriptors.isEmpty()
                || !remoteRepoDescriptors.isEmpty()
                || !localRepoDescriptors.isEmpty());
    }


    private List<RepoDescriptor> getAllDescriptors() {
        List<RepoDescriptor> descriptors = Lists.newArrayList();
        descriptors.addAll(repositoryService.getLocalRepoDescriptors());
        descriptors.addAll(repositoryService.getRemoteRepoDescriptors());
        descriptors.addAll(repositoryService.getVirtualRepoDescriptors());
        return descriptors;
    }

    private List<RepoDescriptor> filterDescriptorsWithCurrentLayout(List<RepoDescriptor> allDescriptors,
            final RepoLayout layout) {
        return Lists.newArrayList(Iterables.filter(allDescriptors, new Predicate<RepoDescriptor>() {
            @Override
            public boolean apply(@Nonnull RepoDescriptor input) {
                return input.getRepoLayout() != null && input.getRepoLayout().equals(layout);
            }
        }));
    }

    private List<RepoDescriptor> filterLocalRepos(List<RepoDescriptor> allDescriptors) {
        List<RepoDescriptor> repoDescriptors = Lists.newArrayList(
                Iterables.filter(allDescriptors, new Predicate<RepoDescriptor>() {
                    @Override
                    public boolean apply(@Nonnull RepoDescriptor input) {
                        return input instanceof LocalRepoDescriptor;
                    }
                }));
        sortDescriptorsAlphabetically(repoDescriptors);
        return repoDescriptors;
    }

    private List<RepoDescriptor> filterRemoteRepos(List<RepoDescriptor> allDescriptors) {
        List<RepoDescriptor> repoDescriptors = Lists.newArrayList(
                Iterables.filter(allDescriptors, new Predicate<RepoDescriptor>() {
                    @Override
                    public boolean apply(@Nonnull RepoDescriptor input) {
                        return input instanceof RemoteRepoDescriptor;
                    }
                }));
        sortDescriptorsAlphabetically(repoDescriptors);
        return repoDescriptors;
    }

    private List<RepoDescriptor> filterVirtualRepos(List<RepoDescriptor> allDescriptors) {
        List<RepoDescriptor> repoDescriptors = Lists.newArrayList(
                Iterables.filter(allDescriptors, new Predicate<RepoDescriptor>() {
                    @Override
                    public boolean apply(@Nonnull RepoDescriptor input) {
                        return !input.isReal();
                    }
                }));
        sortDescriptorsAlphabetically(repoDescriptors);
        return repoDescriptors;
    }

    private void sortDescriptorsAlphabetically(List<RepoDescriptor> descriptors) {
        Collections.sort(descriptors, new Comparator<RepoDescriptor>() {
            @Override
            public int compare(RepoDescriptor o1, RepoDescriptor o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
    }


    @Override
    public String getTitle() {
        return "Repository Associations";
    }
}
