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

package org.artifactory.webapp.wicket.page.config.services;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.index.MavenIndexerService;
import org.artifactory.common.wicket.component.CancelLink;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.dnd.select.sorted.SortedDragDropSelection;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.panel.titled.TitledActionPanel;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.index.IndexerDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.webapp.wicket.components.SortedRepoDragDropSelection;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.page.config.services.cron.CronNextDatePanel;
import org.artifactory.webapp.wicket.util.validation.CronExpValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * General settings (server name, max upload, etc.) configuration panel.
 *
 * @author Yossi Shaul
 */
public class IndexerConfigPanel extends TitledActionPanel {
    private static final Logger log = LoggerFactory.getLogger(IndexerConfigPanel.class);

    @SpringBean
    private CentralConfigService centralConfigService;

    @SpringBean
    private RepositoryService repositoryService;

    @SpringBean
    private MavenIndexerService mavenIndexer;

    private MutableCentralConfigDescriptor centralConfig;

    public IndexerConfigPanel(String id, Form form) {
        super(id);

        centralConfig = centralConfigService.getMutableDescriptor();
        if (centralConfig.getIndexer() == null) {
            centralConfig.setIndexer(new IndexerDescriptor());
        }

        setDefaultModel(new CompoundPropertyModel<>(centralConfig.getIndexer()));

        add(new StyledCheckbox("enabled"));
        add(new SchemaHelpBubble("enabled.help"));


        final TextField<String> cronExpField = new TextField<>("cronExp");
        cronExpField.add(CronExpValidator.getInstance());
        add(cronExpField);
        add(new SchemaHelpBubble("cronExp.help"));

        add(new CronNextDatePanel("cronNextDatePanel", cronExpField));

        // add the run link
        TitledAjaxLink runLink = new TitledAjaxLink("run", "Run Indexing Now") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                BasicStatusHolder statusHolder = new BasicStatusHolder();
                try {
                    BasicStatusHolder status = new BasicStatusHolder();
                    mavenIndexer.scheduleImmediateIndexing(status);
                    if (status.isError()) {
                        error(status.getStatusMsg());
                    } else {
                        info("Indexer was successfully scheduled to run in the background.");
                    }
                } catch (Exception e) {
                    log.error("Could not run indexer.", e);
                    statusHolder.error(e.getMessage(), log);
                    error("Indexer did not run: " + e.getMessage());
                }
            }
        };
        add(runLink);

        List<RepoDescriptor> repoSet = new ArrayList<>();
        repoSet.addAll(repositoryService.getLocalAndRemoteRepoDescriptors());
        repoSet.addAll(getFilteredVirtualRepoDescriptors());

        SortedDragDropSelection<RepoDescriptor> selection =
                new SortedRepoDragDropSelection<RepoDescriptor>("excludedRepositories", repoSet) {
                    @Override
                    protected Collection<RepoDescriptor> createNewSelectionCollection(int length) {
                        //Return a set instead of a list
                        return new TreeSet<>();
                    }
                };
        add(selection);
        add(new SchemaHelpBubble("excludedRepositories.help"));

        addDefaultButton(createSaveButton(form));
        addButton(new CancelLink(form));
    }

    @Override
    public String getTitle() {
        return "Indexer Configuration";
    }

    @Override
    protected Component newToolbar(String id) {
        return new HelpBubble(id, new ResourceModel("indexerConfig.help"));
    }

    private TitledAjaxSubmitLink createSaveButton(Form form) {
        return new TitledAjaxSubmitLink("save", "Save", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                centralConfigService.saveEditedDescriptorAndReload(centralConfig);
                info("Successfully updated Indexer service settings");
                AjaxUtils.refreshFeedback(target);
                target.add(this);
            }
        };
    }

    /**
     * Returns all the virtual repositories, apart from the global one
     *
     * @return Virtual repo descriptor list
     */
    public List<VirtualRepoDescriptor> getFilteredVirtualRepoDescriptors() {
        List<VirtualRepoDescriptor> virtualRepoDescriptors = repositoryService.getVirtualRepoDescriptors();
        List<VirtualRepoDescriptor> descriptorsToAdd = new ArrayList<>();
        for (VirtualRepoDescriptor descriptorToCheck : virtualRepoDescriptors) {
            if (!descriptorToCheck.getKey().equals(VirtualRepoDescriptor.GLOBAL_VIRTUAL_REPO_KEY)) {
                descriptorsToAdd.add(descriptorToCheck);
            }
        }
        return descriptorsToAdd;
    }
}
