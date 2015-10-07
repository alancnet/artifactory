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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.OnKeyUpUpdatingBehavior;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.common.wicket.component.CreateUpdatePanel;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.page.search.BaseSearchPage;
import org.artifactory.webapp.wicket.page.search.bintray.BintraySearchPage;
import org.artifactory.webapp.wicket.page.search.bintray.BintraySearchPanel;
import org.artifactory.webapp.wicket.util.validation.NameValidator;
import org.artifactory.webapp.wicket.util.validation.ReservedPathPrefixValidator;
import org.artifactory.webapp.wicket.util.validation.UniqueXmlIdValidator;
import org.artifactory.webapp.wicket.util.validation.XsdNCNameValidator;

import java.util.List;

/**
 * Base panel for repositories configuration.
 *
 * @author Yossi Shaul
 */
public abstract class RepoConfigCreateUpdatePanel<E extends RepoDescriptor> extends CreateUpdatePanel<E> {

    protected final CachingDescriptorHelper cachingDescriptorHelper;
    @SpringBean
    protected AddonsManager addons;
    @SpringBean
    protected CentralConfigService centralConfigService;

    protected RepoConfigCreateUpdatePanel(CreateUpdateAction action, final E repoDescriptor,
            CachingDescriptorHelper cachingDescriptorHelper) {
        super(action, repoDescriptor);

        this.cachingDescriptorHelper = cachingDescriptorHelper;
        form.setOutputMarkupId(true);
        add(new CssClass("repo-config"));
        setWidth(650);
        TitledBorder repoConfigBorder = new TitledBorder("repoConfigBorder");
        TextField<String> repoKeyField = new TextField<>("key");
        setDefaultFocusField(repoKeyField);

        boolean create = isCreate();
        repoKeyField.setEnabled(create);// don't allow key update
        if (create) {
            repoKeyField.add(new NameValidator("Invalid repository key '%s'."));
            repoKeyField.add(new XsdNCNameValidator("Invalid repository key '%s'."));
            repoKeyField.add(new UniqueXmlIdValidator(cachingDescriptorHelper.getModelMutableDescriptor()));
            repoKeyField.add(new ReservedPathPrefixValidator());
            // we don't use a delay since it causes an error when the panel is closed while the focus is on the repo
            // key field
            repoKeyField.add(new OnKeyUpUpdatingBehavior(0) {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    // TODO: [by sy] send an event to interested components? (nuget panel)
                }
            });
        }

        repoConfigBorder.add(repoKeyField);
        repoConfigBorder.add(new SchemaHelpBubble("key.help"));

        repoConfigBorder.add(new RepoTabbedPanel("repoConfigTabbedPanel", getConfigurationTabs()));

        form.add(repoConfigBorder);

        // Cancel button
        form.add(getCloseLink());

        // Submit button
        TitledAjaxSubmitLink submit = createSubmitButton();
        form.add(submit);
        form.add(new DefaultButtonBehavior(submit));

        // test button (when relevant)
        TitledAjaxSubmitLink test = createTestButton();
        form.add(test);
        form.add(new DefaultButtonBehavior(test));

        add(form);

        bindHeightTo("modalScroll");
    }

    protected abstract List<ITab> getConfigurationTabs();

    protected TitledAjaxLink getCloseLink() {

        TitledAjaxLink titledAjaxLink = new TitledAjaxLink("cancel", "Cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                syncSaveAndCurrentLocalReplicationData();
                ModalHandler.closeCurrent(target);
            }
        };
        return titledAjaxLink;
    }

    /**
     * update repo related property in db in case repo data (replication and etc)
     * has been changed or deleted
     */
    public void updateRepositoryReplicationProperties() {

    }

    /**
     * sync local with saved replication data  after removing replication data without saving it
     */
    private void syncSaveAndCurrentLocalReplicationData() {
        List<LocalReplicationDescriptor> savedLocalReplicationDescriptorList = cachingDescriptorHelper.getSavedMutableDescriptor().getLocalReplications();
        List<LocalReplicationDescriptor> localReplicationDescriptorList = cachingDescriptorHelper.getModelMutableDescriptor().getLocalReplications();
        if (localReplicationDescriptorList.size() != savedLocalReplicationDescriptorList.size()) {
            cachingDescriptorHelper.getModelMutableDescriptor().setLocalReplications(
                    savedLocalReplicationDescriptorList);
        }
    }

    public abstract void addAndSaveDescriptor(E repoDescriptor);

    public abstract void saveEditDescriptor(E repoDescriptor);

    @SuppressWarnings({"unchecked"})
    protected E getRepoDescriptor() {
        return (E) form.getDefaultModelObject();
    }

    private TitledAjaxSubmitLink createSubmitButton() {
        String submitCaption = isCreate() ? "Create" : "Save";
        return new TitledAjaxSubmitLink("submit", submitCaption, form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                E repoDescriptor = getRepoDescriptor();
                if (StringUtils.isBlank(repoDescriptor.getKey())) {
                    error("Please enter the repository key.");
                    AjaxUtils.refreshFeedback(target);
                    return;
                }
                if (!validate(repoDescriptor)) {
                    AjaxUtils.refreshFeedback();
                    return;
                }
                if (isCreate()) {
                    addAndSaveDescriptor(repoDescriptor);
                    getPage().info("Successfully created repository '" + repoDescriptor.getKey() + "'");
                } else {
                    saveEditDescriptor(repoDescriptor);
                    getPage().info("Successfully updated repository '" + repoDescriptor.getKey() + "'");
                    updateRepositoryReplicationProperties();
                }

                cachingDescriptorHelper.reset();
                if (getPage() instanceof RepositoryConfigPage) {
                    ((RepositoryConfigPage) getPage()).refresh(target);
                }
                if (getPage() instanceof BintraySearchPage) {
                    ((BintraySearchPanel) getPage().get(BaseSearchPage.SEARCH_TABS).get("panel")).refresh(target);
                }
                AjaxUtils.refreshFeedback(target);
                close(target);
            }
        };
    }

    protected TitledAjaxSubmitLink createTestButton() {
        // create a dummy hidden link
        TitledAjaxSubmitLink hiddenLink = new TitledAjaxSubmitLink("test") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
            }
        };
        hiddenLink.setVisible(false);
        return hiddenLink;
    }

    protected abstract boolean validate(E repoDescriptor);

    protected CachingDescriptorHelper getCachingDescriptorHelper() {
        return cachingDescriptorHelper;
    }

    @Override
    public void onClose(AjaxRequestTarget target) {
        if (!isCreate()) {
            // if not create, reload the repo from the latest config to handle cancel actions
            getCachingDescriptorHelper().reloadRepository(getRepoDescriptor().getKey());
        }
        // reset caching descriptor caches
        cachingDescriptorHelper.reset();
    }

}
