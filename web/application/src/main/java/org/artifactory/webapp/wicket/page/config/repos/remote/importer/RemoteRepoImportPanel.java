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

package org.artifactory.webapp.wicket.page.config.repos.remote.importer;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.ajax.ConfirmationAjaxCallDecorator;
import org.artifactory.common.wicket.ajax.NoAjaxIndicatorDecorator;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.modal.links.ModalCloseLink;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.component.table.columns.TextFieldColumn;
import org.artifactory.common.wicket.component.table.columns.TooltipLabelColumn;
import org.artifactory.common.wicket.component.table.columns.checkbox.SelectAllCheckboxColumn;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.common.wicket.util.ComponentPersister;
import org.artifactory.common.wicket.util.ListPropertySorter;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.wicket.page.config.repos.CachingDescriptorHelper;
import org.artifactory.webapp.wicket.page.config.repos.RepositoryConfigPage;
import org.artifactory.webapp.wicket.util.validation.NameValidator;
import org.artifactory.webapp.wicket.util.validation.UriValidator;
import org.artifactory.webapp.wicket.util.validation.XsdNCNameValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.artifactory.common.wicket.component.modal.ModalHandler.resizeAndCenterCurrent;
import static org.artifactory.common.wicket.util.ComponentPersister.setPersistent;
import static org.artifactory.descriptor.property.PropertySet.ARTIFACTORY_RESERVED_PROP_SET;

/**
 * Enables the user to import shared remote repository settings from a remote artifactory instance
 *
 * @author Noam Y. Tenne
 */
public class RemoteRepoImportPanel extends BaseModalPanel {
    private static final Logger log = LoggerFactory.getLogger(RemoteRepoImportPanel.class);

    @SpringBean
    private CentralConfigService centralConfigService;

    @SpringBean
    private RepositoryService repositoryService;

    private RepoDataProvider provider;
    private SortableTable repoTable;

    @WicketProperty
    private String url;
    private TitledAjaxSubmitLink importButton;

    public RemoteRepoImportPanel(CachingDescriptorHelper cachingDescriptorHelper) {
        setWidth(740);
        add(new CssClass("import-remote-repos"));
        Form loadForm = new SecureForm("loadForm");
        add(loadForm);

        MarkupContainer loadBorder = new TitledBorder("loadBorder");
        loadForm.add(loadBorder);

        loadBorder.add(new HelpBubble("urlHelp",
                "Enter the base URL of another Artifactory server you want to import repository definitions from."));
        FormComponent<String> urlTextField = new TextField<>("url", new PropertyModel<String>(this, "url"));
        urlTextField.add(new UriValidator("http", "https"));
        setPersistent(urlTextField);
        urlTextField.setOutputMarkupId(true);
        urlTextField.setRequired(true);
        urlTextField.setDefaultModelObject("http://repo.jfrog.org/artifactory");
        loadBorder.add(urlTextField);
        loadBorder.add(getLoadButton(loadForm));

        Form listForm = new SecureForm("listForm");
        add(listForm);

        MarkupContainer listBorder = new TitledBorder("listBorder");
        listForm.add(listBorder);
        createRepositoryList(listBorder);

        add(new ModalCloseLink("cancel"));
        //Submit button
        importButton = getImportButton(cachingDescriptorHelper, listForm);
        importButton.setOutputMarkupId(true);
        add(importButton);
        listForm.add(new DefaultButtonBehavior(importButton));
    }

    @Override
    protected void onBeforeRender() {
        ComponentPersister.loadChildren(this);
        super.onBeforeRender();
    }

    @Override
    public String getTitle() {
        return "Import Remote Repositories";
    }

    /**
     * Constructs and returns the repository list loading button
     *
     * @param form Form to associate button to
     * @return Load button
     */
    private Component getLoadButton(Form form) {
        return new TitledAjaxSubmitLink("load", "Load", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                ComponentPersister.saveChildren(RemoteRepoImportPanel.this);
                Session.get().cleanupFeedbackMessages();

                //Reset the provider data
                provider.setData(Lists.<ImportableRemoteRepo>newArrayList());
                try {
                    List<ImportableRemoteRepo> importableRepoList = getSharedRemoteRepos();
                    if (importableRepoList.isEmpty()) {
                        warn("No shared repositories could be found.");
                        return;
                    }
                    provider.setData(importableRepoList);
                    target.add(repoTable);
                    resizeAndCenterCurrent();
                } catch (Exception e) {
                    error("An error occurred while locating shared repositories: " + e.getMessage());
                } finally {
                    AjaxUtils.refreshFeedback(target);
                }
            }
        };
    }

    /**
     * Constructs and returns the repository import button
     *
     * @param form Form to associate button to
     * @return Import button
     */
    private TitledAjaxSubmitLink getImportButton(
            final CachingDescriptorHelper cachingDescriptorHelper, Form form) {
        return new TitledAjaxSubmitLink("import", "Import", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                ComponentPersister.saveChildren(RemoteRepoImportPanel.this);

                try {
                    Session.get().cleanupFeedbackMessages();
                    List<ImportableRemoteRepo> importableRepos = provider.getData();

                    MutableCentralConfigDescriptor mutableCentralConfigDescriptor =
                            cachingDescriptorHelper.getModelMutableDescriptor();

                    //Validate selected repo lists
                    Map<String, RemoteRepoDescriptor> reposToImport =
                            validatedAndReturnSelection(importableRepos);

                    //Add new imported repositories if the list isn't empty
                    if (!reposToImport.isEmpty()) {

                        int selectedRepoCount = reposToImport.size();
                        Map<String, RemoteRepoDescriptor> existingRepos =
                                mutableCentralConfigDescriptor.getRemoteRepositoriesMap();
                        Collection<RepoPath> reposToZap = new ArrayList<>();

                        //Remove existing remote repositories that will be re-imported
                        for (RemoteRepoDescriptor repoToImport : reposToImport.values()) {
                            String key = repoToImport.getKey();
                            existingRepos.remove(key);

                            //Add the repo to the zap list
                            if (repoToImport.isStoreArtifactsLocally()) {
                                reposToZap.add(InternalRepoPathFactory.create(key + "-cache", ""));
                            }
                        }
                        // add the Artifactory reserved property set if it exists and is not associated with the new repo.
                        if (mutableCentralConfigDescriptor.isPropertySetExists(ARTIFACTORY_RESERVED_PROP_SET)) {
                            PropertySet propertySet = getArtifactoryPropertySet(mutableCentralConfigDescriptor);
                            for (RemoteRepoDescriptor descriptor : reposToImport.values()) {
                                if (!descriptor.isPropertySetExists(ARTIFACTORY_RESERVED_PROP_SET)) {
                                    descriptor.addPropertySet(propertySet);
                                }
                            }
                        }
                        //Re-Add existing repositories to the import list
                        reposToImport.putAll(existingRepos);
                        mutableCentralConfigDescriptor.setRemoteRepositoriesMap(reposToImport);
                        try {
                            cachingDescriptorHelper.syncAndSaveRemoteRepositories();
                            getPage().info(String.format("Successfully imported %s remote repository definitions.",
                                    selectedRepoCount));

                        } catch (Exception e) {
                            String message = "Error occurred will saving new imported repositories";
                            getPage().error(message);
                            log.error(message, e);
                        }

                        //Zap all the new repositories
                        for (RepoPath repoToZap : reposToZap) {
                            repositoryService.zap(repoToZap);
                        }

                        ((RepositoryConfigPage) getPage()).refresh(target);
                        close(target);
                    }
                } finally {
                    AjaxUtils.refreshFeedback(target);
                }
            }

            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                //If a repo which key already exists as another remote repo key is selected, warn
                List<ImportableRemoteRepo> importableRepos = provider.getData();
                for (ImportableRemoteRepo importableRepo : importableRepos) {
                    if (importableRepo.isSelected() && importableRepo.isExistsAsRemote()) {
                        return new ConfirmationAjaxCallDecorator(getString("existsAsRemoteWarn"));
                    }
                }
                return super.getAjaxCallDecorator();
            }
        };
    }

    private PropertySet getArtifactoryPropertySet(CentralConfigDescriptor centralConfig) {
        List<PropertySet> propertySets = centralConfig.getPropertySets();
        PropertySet propertySet = Iterables.find(propertySets, new Predicate<PropertySet>() {
            @Override
            public boolean apply(PropertySet input) {
                return input.getName().equals(ARTIFACTORY_RESERVED_PROP_SET);
            }
        });
        return propertySet;
    }

    /**
     * Validates and returns the selected repositories out of the list
     *
     * @param importableRepos List of importable repositories
     * @return repos map
     */
    private Map<String, RemoteRepoDescriptor> validatedAndReturnSelection(Collection<ImportableRemoteRepo>
            importableRepos) {
        Map<String, RemoteRepoDescriptor> map = Maps.newLinkedHashMap();

        if (importableRepos.isEmpty()) {
            error("Please select at least one repository to import.");
            return map;
        }

        //If a repo which key already exists as another local\virtual repo key is selected, throw an error
        for (ImportableRemoteRepo importableRepo : importableRepos) {
            if (importableRepo.isSelected()) {
                if (importableRepo.isExistsAsLocal() || importableRepo.isExistsAsVirtual()) {
                    error(getString("existsAsLocalOrVirtualWarn"));
                    map.clear();
                    return map;
                }

                map.put(importableRepo.getRepoKey(), importableRepo.getRepoDescriptor());
            }
        }

        if (map.isEmpty()) {
            error("Please select at least one repository to import.");
        }

        return map;
    }

    /**
     * Returns the list of shared remote repositories from the entered URL
     *
     * @return List of importable repositories
     */
    private List<ImportableRemoteRepo> getSharedRemoteRepos() {
        //Append headers
        Map<String, String> headersMap = WicketUtils.getHeadersMap();
        List<RemoteRepoDescriptor> remoteRepoList = repositoryService.getSharedRemoteRepoConfigs(url, headersMap);
        List<ImportableRemoteRepo> importableRepoList = Lists.newArrayList();

        //Indicate if an importable repository key already exists as a local\remote repository's key
        for (RemoteRepoDescriptor remoteRepoDescriptor : remoteRepoList) {
            ImportableRemoteRepo importableRemoteRepo = new ImportableRemoteRepo(remoteRepoDescriptor);

            validateRepoKey(importableRemoteRepo);

            importableRepoList.add(importableRemoteRepo);
        }
        return importableRepoList;
    }

    /**
     * Validate the importable repo key
     *
     * @param importableRemoteRepo Importable repo to validate
     */
    private void validateRepoKey(ImportableRemoteRepo importableRemoteRepo) {
        //Indicate if the key already exists as any type
        boolean existsAsLocal = repositoryService.localRepoDescriptorByKey(importableRemoteRepo.getRepoKey()) != null;
        boolean existsAsRemote = false;
        if (!existsAsLocal) {
            existsAsRemote = repositoryService.remoteRepoDescriptorByKey(importableRemoteRepo.getRepoKey()) != null;
        }
        boolean existsAsVirtual = false;
        if (!existsAsLocal && !existsAsRemote) {
            existsAsVirtual = repositoryService.virtualRepoDescriptorByKey(importableRemoteRepo.getRepoKey()) != null;
        }
        importableRemoteRepo.setExistsAsLocal(existsAsLocal);
        importableRemoteRepo.setExistsAsRemote(existsAsRemote);
        importableRemoteRepo.setExistsAsVirtual(existsAsVirtual);
    }

    /**
     * Constructs the repository list
     *
     * @param listBorder Border to add the list to
     */
    private void createRepositoryList(MarkupContainer listBorder) {
        provider = new RepoDataProvider();
        repoTable = new SortableTable<>("repoTable", getColumns(), provider, 10);
        repoTable.setOutputMarkupId(true);
        listBorder.add(repoTable);
    }

    /**
     * Returns a list of columns for the repository list
     *
     * @return Columns list
     */
    private List<IColumn<ImportableRemoteRepo>> getColumns() {
        List<IColumn<ImportableRemoteRepo>> columns = Lists.newArrayList();
        columns.add(new SelectAllCheckboxColumn<ImportableRemoteRepo>("", "selected", null) {
            @Override
            protected void onUpdate(FormComponent checkbox, ImportableRemoteRepo rowObject, boolean value,
                    AjaxRequestTarget target) {
                super.onUpdate(checkbox, rowObject, value, target);
                //On each update, refresh import button to customize the warning messages of the call decorator
                target.add(importButton);
            }

            @Override
            protected void onSelectAllUpdate(AjaxRequestTarget target) {
                target.add(importButton);
            }
        });
        columns.add(new KeyTextFieldColumn());
        columns.add(new TooltipLabelColumn<ImportableRemoteRepo>(Model.of("URL"), "repoUrl", "repoUrl", 50));
        columns.add(new TooltipLabelColumn<ImportableRemoteRepo>(
                Model.of("Description"), "repoDescription", "repoDescription", 25));
        return columns;
    }

    /**
     * Editable repo key text field column
     */
    private class KeyTextFieldColumn extends TextFieldColumn<ImportableRemoteRepo> {
        private KeyTextFieldColumn() {
            super("Key", "repoKey");
        }

        @Override
        protected TextField<String> newTextField(String id, IModel<String> valueModel,
                final ImportableRemoteRepo rowObject) {
            TextField<String> textField = super.newTextField(id, valueModel, rowObject);
            textField.setLabel(Model.of("Key"));
            textField.setOutputMarkupId(true);
            textField.setRequired(true);
            textField.add(new NameValidator("Invalid repository key '%s'."));
            textField.add(new XsdNCNameValidator("Invalid repository key '%s'."));
            textField.add(new AjaxFormComponentUpdatingBehavior("onkeyup") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    validateRepoKey(rowObject);
                    //On each update, refresh import button to customize the warning messages of the call decorator
                    target.add(importButton);
                }

                @Override
                protected void onError(AjaxRequestTarget target, RuntimeException e) {
                    super.onError(target, e);
                    AjaxUtils.refreshFeedback();
                }

                @Override
                protected IAjaxCallDecorator getAjaxCallDecorator() {
                    return new NoAjaxIndicatorDecorator();
                }
            });
            return textField;
        }
    }

    /**
     * The importable repository data provider
     */
    private static class RepoDataProvider extends SortableDataProvider<ImportableRemoteRepo> {

        /**
         * Main content list
         */
        private List<ImportableRemoteRepo> list = Lists.newArrayList();

        /**
         * Default constructor
         */
        private RepoDataProvider() {
            setSort("repoKey", SortOrder.ASCENDING);
        }

        @Override
        public Iterator<ImportableRemoteRepo> iterator(int first, int count) {
            ListPropertySorter.sort(list, getSort());
            List<ImportableRemoteRepo> listToReturn = list.subList(first, first + count);
            return listToReturn.iterator();
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public IModel<ImportableRemoteRepo> model(ImportableRemoteRepo object) {
            return new Model<>(object);
        }

        /**
         * Returns the data list
         *
         * @return List of importable repositories
         */
        public List<ImportableRemoteRepo> getData() {
            return list;
        }

        /**
         * Sets the given data to the list
         *
         * @param dataToSet List of importable repositories to set
         */
        private void setData(Collection<ImportableRemoteRepo> dataToSet) {
            list.clear();
            list.addAll(dataToSet);
        }
    }
}
