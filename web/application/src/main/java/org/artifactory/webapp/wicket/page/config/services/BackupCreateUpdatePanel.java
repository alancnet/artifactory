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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.BackupService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonStyleModel;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.common.wicket.component.CreateUpdatePanel;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.file.browser.button.FileBrowserButton;
import org.artifactory.common.wicket.component.file.path.PathAutoCompleteTextField;
import org.artifactory.common.wicket.component.file.path.PathMask;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.modal.links.ModalCloseLink;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.security.AccessLogger;
import org.artifactory.webapp.wicket.components.SortedRepoDragDropSelection;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.page.config.services.cron.CronNextDatePanel;
import org.artifactory.webapp.wicket.util.validation.CronExpValidator;
import org.artifactory.webapp.wicket.util.validation.NameValidator;
import org.artifactory.webapp.wicket.util.validation.UniqueXmlIdValidator;
import org.artifactory.webapp.wicket.util.validation.XsdNCNameValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Backups configuration panel.
 *
 * @author Yossi Shaul
 */
public class BackupCreateUpdatePanel extends CreateUpdatePanel<BackupDescriptor> {
    private static final Logger log = LoggerFactory.getLogger(BackupCreateUpdatePanel.class);

    @SpringBean
    private BackupService backupService;

    @SpringBean
    private CentralConfigService centralConfigService;

    @SpringBean
    private RepositoryService repositoryService;

    @WicketProperty
    private boolean createIncrementalBackup;

    final TextField retentionHoursField;

    final StyledCheckbox createIncremental;
    private StyledCheckbox createArchiveCheckbox;

    private FileBrowserButton browserButton;
    private PathAutoCompleteTextField backupDir;
    private BackupsListPanel backupsListPanel;
    private TitledAjaxSubmitLink runNowButton;
    private StyledCheckbox enabledCheckBox;

    public BackupCreateUpdatePanel(CreateUpdateAction action, BackupDescriptor backupDescriptor,
            BackupsListPanel backupsListPanel) {
        super(action, backupDescriptor);
        this.backupsListPanel = backupsListPanel;
        createIncrementalBackup = backupDescriptor.isIncremental() && !backupDescriptor.isCreateArchive();
        setWidth(560);

        add(form);

        TitledBorder simpleFields = new TitledBorder("simple");
        form.add(simpleFields);

        // Backup key
        RequiredTextField<String> keyField = new RequiredTextField<>("key");
        setDefaultFocusField(keyField);
        keyField.setEnabled(isCreate());// don't allow key update
        if (isCreate()) {
            keyField.add(new NameValidator("Invalid backup key '%s'"));
            keyField.add(new XsdNCNameValidator("Invalid backup key '%s'"));
            keyField.add(new UniqueXmlIdValidator(backupsListPanel.getMutableDescriptor()));
        }
        simpleFields.add(keyField);
        simpleFields.add(new SchemaHelpBubble("key.help"));

        enabledCheckBox = new StyledCheckbox("enabled");
        enabledCheckBox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                runNowButton.setEnabled(enabledCheckBox.isChecked());
                target.add(runNowButton);
            }
        });
        simpleFields.add(enabledCheckBox);

        final TextField<String> cronExpField = new TextField<>("cronExp");
        cronExpField.add(CronExpValidator.getInstance());
        simpleFields.add(cronExpField);
        simpleFields.add(new SchemaHelpBubble("cronExp.help"));

        simpleFields.add(new CronNextDatePanel("cronNextDatePanel", cronExpField));

        PropertyModel<File> pathModel = new PropertyModel<>(backupDescriptor, "dir");

        backupDir = new PathAutoCompleteTextField("dir", pathModel);
        backupDir.setMask(PathMask.FOLDERS);
        simpleFields.add(backupDir);
        simpleFields.add(new SchemaHelpBubble("dir.help"));

        browserButton = new FileBrowserButton("browseButton", pathModel) {
            @Override
            protected void onOkClicked(AjaxRequestTarget target) {
                super.onOkClicked(target);
                target.add(backupDir);
            }
        };
        browserButton.setOutputMarkupId(true);
        simpleFields.add(browserButton);

        TitledBorder advancedFields = new TitledBorder("advanced");
        form.add(advancedFields);

        retentionHoursField = new TextField("retentionPeriodHours");
        retentionHoursField.setOutputMarkupId(true);
        retentionHoursField.setEnabled(!createIncrementalBackup);
        advancedFields.add(retentionHoursField);
        advancedFields.add(new SchemaHelpBubble("retentionPeriodHours.help"));

        createArchiveCheckbox = new StyledCheckbox("createArchive");
        createArchiveCheckbox.setOutputMarkupId(true);
        createArchiveCheckbox.setEnabled(!createIncrementalBackup);
        createArchiveCheckbox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                boolean isCreateArchiveChecked = createArchiveCheckbox.isChecked();
                createIncremental.setEnabled(!isCreateArchiveChecked);
                if (isCreateArchiveChecked) {
                    createIncremental.setDefaultModelObject(Boolean.FALSE);
                }
                target.add(createIncremental);
            }
        });
        advancedFields.add(createArchiveCheckbox);
        advancedFields.add(new SchemaHelpBubble("createArchive.help"));

        advancedFields.add(new StyledCheckbox("sendMailOnError"));
        advancedFields.add(new SchemaHelpBubble("sendMailOnError.help"));

        advancedFields.add(new StyledCheckbox("excludeBuilds"));
        advancedFields.add(new SchemaHelpBubble("excludeBuilds.help"));

        advancedFields.add(new StyledCheckbox("excludeNewRepositories"));
        advancedFields.add(new SchemaHelpBubble("excludeNewRepositories.help"));

        createIncremental = new StyledCheckbox("createIncrementalBackup",
                new PropertyModel<Boolean>(this, "createIncrementalBackup"));
        createIncremental.setOutputMarkupId(true);
        createIncremental.setRequired(false);
        createIncremental.setEnabled(!backupDescriptor.isCreateArchive());
        createIncremental.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                boolean isIncrementalChecked = createIncremental.isChecked();
                createArchiveCheckbox.setEnabled(!isIncrementalChecked);
                retentionHoursField.setEnabled(!isIncrementalChecked);
                if (isIncrementalChecked) {
                    createArchiveCheckbox.setDefaultModelObject(Boolean.FALSE);
                    retentionHoursField.setDefaultModelObject("0");
                }
                target.add(retentionHoursField);
                target.add(createArchiveCheckbox);
            }
        });
        advancedFields.add(createIncremental);

        List<RepoDescriptor> repos = repositoryService.getLocalAndRemoteRepoDescriptors();
        advancedFields.add(new SortedRepoDragDropSelection<>("excludedRepositories", repos));
        advancedFields.add(new SchemaHelpBubble("excludedRepositories.help"));

        // Cancel button
        form.add(new ModalCloseLink("cancel"));

        // Submit button
        TitledAjaxSubmitLink submit = createSubmitButton(backupsListPanel);
        form.add(submit);
        form.add(new DefaultButtonBehavior(submit));

        runNowButton = createRunNowButton();
        runNowButton.setOutputMarkupId(true);
        runNowButton.setEnabled(backupDescriptor.isEnabled());
        runNowButton.add(new CssClass(new DefaultButtonStyleModel(runNowButton)));
        form.add(runNowButton);
    }

    @Override
    public void onClose(AjaxRequestTarget target) {
        backupsListPanel.refresh(target);
    }

    private TitledAjaxSubmitLink createSubmitButton(final BackupsListPanel backupsListPanel) {
        String submitCaption = isCreate() ? "Create" : "Save";
        return new TitledAjaxSubmitLink("submit", submitCaption, form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                if (StringUtils.isBlank(entity.getCronExp())) {
                    error("A Cron Expression is required.");
                    AjaxUtils.refreshFeedback(target);
                    return;
                }
                MutableCentralConfigDescriptor configDescriptor = backupsListPanel.getMutableDescriptor();
                if (isCreate()) {
                    configDescriptor.addBackup(entity);
                    centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
                    String message = "Successfully created backup '" + entity.getKey() + "'";
                    AccessLogger.created(message);
                    getPage().info(message);
                } else {
                    centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
                    String message = "Successfully created backup '" + entity.getKey() + "'";
                    AccessLogger.updated(message);
                    getPage().info(message);
                }
                AjaxUtils.refreshFeedback(target);
                target.add(backupsListPanel);
                close(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                super.onError(target);
                IModel dirModal = browserButton.getDefaultModel();
                backupDir.setDefaultModel(dirModal);
            }
        };
    }

    private TitledAjaxSubmitLink createRunNowButton() {
        return new TitledAjaxSubmitLink("runNow", "Run Now", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                BasicStatusHolder statusHolder = new BasicStatusHolder();
                try {
                    backupService.scheduleImmediateSystemBackup(entity, statusHolder);
                    if (statusHolder.isError()) {
                        error(statusHolder.getStatusMsg());
                    } else {
                        info("System backup was successfully scheduled to run in the background.");
                        AjaxUtils.refreshFeedback(target);
                    }
                } catch (Exception e) {
                    String errorMessage = "Could not run system backup '" + entity.getKey() + "': " + e.getMessage();
                    statusHolder.error(errorMessage, e, log);
                    error(errorMessage);
                }
                AjaxUtils.refreshFeedback(target);
            }
        };
    }
}
