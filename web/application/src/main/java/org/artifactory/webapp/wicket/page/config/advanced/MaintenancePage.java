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

package org.artifactory.webapp.wicket.page.config.advanced;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.cleanup.ArtifactCleanupService;
import org.artifactory.api.repo.cleanup.VirtualCacheCleanupService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.ajax.ConfirmationAjaxCallDecorator;
import org.artifactory.common.wicket.component.CancelLink;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.descriptor.cleanup.CleanupConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.gc.GcConfigDescriptor;
import org.artifactory.descriptor.quota.QuotaConfigDescriptor;
import org.artifactory.storage.StorageService;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.page.config.services.cron.CronNextDatePanel;
import org.artifactory.webapp.wicket.util.validation.CronExpValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays the different maintenance controls to the user
 *
 * @author Noam Tenne
 */
@AuthorizeInstantiation(AuthorizationService.ROLE_ADMIN)
public class MaintenancePage extends AuthenticatedPage {

    private static final Logger log = LoggerFactory.getLogger(MaintenancePage.class);

    @SpringBean
    private CentralConfigService centralConfigService;

    @SpringBean
    private StorageService storageService;

    @SpringBean
    private ArtifactCleanupService artifactCleanupService;

    @SpringBean
    private VirtualCacheCleanupService virtualCacheCleanupService;

    private GcConfigDescriptor gcConfigDescriptor;
    private CleanupConfigDescriptor cleanupConfigDescriptor;
    private CleanupConfigDescriptor virtualCacheCleanupConfigDescriptor;
    private QuotaConfigDescriptor quotaConfigDescriptor;

    public MaintenancePage() {
        Form form = new SecureForm("form");
        form.setOutputMarkupId(true);
        add(form);

        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        gcConfigDescriptor = mutableDescriptor.getGcConfig();
        cleanupConfigDescriptor = mutableDescriptor.getCleanupConfig();
        virtualCacheCleanupConfigDescriptor = mutableDescriptor.getVirtualCacheCleanupConfig();
        quotaConfigDescriptor = mutableDescriptor.getQuotaConfig();
        if (quotaConfigDescriptor == null) {
            quotaConfigDescriptor = new QuotaConfigDescriptor();
            quotaConfigDescriptor.setDiskSpaceLimitPercentage(95);
            quotaConfigDescriptor.setDiskSpaceWarningPercentage(85);
        }

        setOutputMarkupId(true);
        addStorageMaintenance();
        addGarbageCollectorMaintenance(form);
        addArtifactsCleanupMaintenance(form);
        addVirtualCleanupMaintenance(form);
        addDiskQuotaManagement(form);
        addButtons(form);
    }

    private void addButtons(Form form) {
        add(new TitledAjaxSubmitLink("saveButton", "Save", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
                mutableDescriptor.setGcConfig(gcConfigDescriptor);
                mutableDescriptor.setCleanupConfig(cleanupConfigDescriptor);
                mutableDescriptor.setQuotaConfig(quotaConfigDescriptor);
                mutableDescriptor.setVirtualCacheCleanupConfig(virtualCacheCleanupConfigDescriptor);
                centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
                info("Maintenance settings were successfully saved.");
                AjaxUtils.refreshFeedback();
            }
        });
        add(new CancelLink("cancel", form) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(MaintenancePage.class);
            }
        });
    }

    /**
     * Add the storage maintenance control to the page
     */
    private void addStorageMaintenance() {
        TitledBorder border = new TitledBorder("storage");
        add(border);

        // add the compress link
        TitledAjaxLink compressLink = new TitledAjaxLink("compress", "Compress the Internal Database") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                BasicStatusHolder statusHolder = new BasicStatusHolder();
                try {
                    storageService.compress(statusHolder);
                } catch (Exception e) {
                    statusHolder.error(e.getMessage(), log);
                } finally {
                    if (statusHolder.isError()) {
                        error("Failed to compress database: " + statusHolder.getLastError().getMessage());
                    } else {
                        info("Database successfully compressed.");
                    }
                }
            }

            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new ConfirmationAjaxCallDecorator(super.getAjaxCallDecorator(),
                        "Are you sure you want to compress the internal database? (Overall performance will degrade " +
                                "until compression completes).");
            }
        };
        border.add(compressLink);
        HelpBubble compressHelpBubble = new HelpBubble("compressHelp", new ResourceModel("compressHelp"));
        border.add(compressHelpBubble);

        // add the prune link
        TitledAjaxLink pruneLink = new TitledAjaxLink("prune", "Prune Unreferenced Data") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                BasicStatusHolder statusHolder = new BasicStatusHolder();
                storageService.pruneUnreferencedFileInDataStore(statusHolder);
                if (statusHolder.isError()) {
                    error("Pruning unreferenced data completed with an error:\n" +
                            statusHolder.getLastError().getMessage() + ".");
                } else {
                    info("Pruning unreferenced data completed successfully!\n" + statusHolder.getStatusMsg());
                }
            }
        };
        border.add(pruneLink);
        HelpBubble pruneHelpBubble = new HelpBubble("pruneHelp", new ResourceModel("pruneHelp"));
        border.add(pruneHelpBubble);

        // Compress only valid for Derby DB
        boolean isDerbyUsed = storageService.isDerbyUsed();
        compressLink.setVisible(isDerbyUsed);
        compressHelpBubble.setVisible(isDerbyUsed);
    }

    private void addDiskQuotaManagement(Form form) {
        final Border quotaBorder = new TitledBorder("quotaBorder", new CompoundPropertyModel(quotaConfigDescriptor));
        form.add(quotaBorder);

        final StyledCheckbox enableCheckbox = new StyledCheckbox("enabled");
        enableCheckbox.setOutputMarkupId(true);
        quotaBorder.add(enableCheckbox.setTitle("Enable Quota Control"));
        quotaBorder.add(new SchemaHelpBubble("enabled.help"));
        boolean quotaEnabled = quotaConfigDescriptor.isEnabled();

        final TextField<Integer> diskSpaceLimitPercentage = new TextField<>("diskSpaceLimitPercentage",
                Integer.class);
        diskSpaceLimitPercentage.add(new RangeValidator<>(0, 99));
        diskSpaceLimitPercentage.setEnabled(quotaEnabled).setOutputMarkupId(true);
        quotaBorder.add(diskSpaceLimitPercentage);
        quotaBorder.add(new SchemaHelpBubble("diskSpaceLimitPercentage.help"));

        final TextField<Integer> diskSpaceWarningPercentage = new TextField<>("diskSpaceWarningPercentage",
                Integer.class);
        diskSpaceWarningPercentage.add(new RangeValidator<>(0, 99));
        diskSpaceWarningPercentage.setEnabled(quotaEnabled).setOutputMarkupId(true);
        quotaBorder.add(diskSpaceWarningPercentage);
        quotaBorder.add(new SchemaHelpBubble("diskSpaceWarningPercentage.help"));

        enableCheckbox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @Override
            protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
                boolean enabled = enableCheckbox.isChecked();
                diskSpaceLimitPercentage.setEnabled(enabled);
                diskSpaceWarningPercentage.setEnabled(enabled);
                ajaxRequestTarget.add(diskSpaceLimitPercentage, diskSpaceWarningPercentage);
            }
        });
    }

    private void addGarbageCollectorMaintenance(Form form) {
        final Border gcBorder = new TitledBorder("gcBorder", new CompoundPropertyModel(gcConfigDescriptor));
        form.add(gcBorder);
        TextField<String> cronExpTextField = new TextField<>("cronExp");
        cronExpTextField.setRequired(true);
        cronExpTextField.add(CronExpValidator.getInstance());
        gcBorder.add(cronExpTextField);
        gcBorder.add(new SchemaHelpBubble("cronExp.help"));
        gcBorder.add(new CronNextDatePanel("cronNextDatePanel", cronExpTextField));

        TitledAjaxLink collectLink = new TitledAjaxLink("collect", "Run Now") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                BasicStatusHolder statusHolder = new BasicStatusHolder();
                storageService.callManualGarbageCollect(statusHolder);
                if (statusHolder.isError()) {
                    error("Could not run the garbage collector: " + statusHolder.getLastError().getMessage() + ".");
                } else {
                    info("Garbage collector was successfully scheduled to run in the background.");
                }
            }
        };
        gcBorder.add(collectLink);
        HelpBubble gcHelpBubble = new HelpBubble("gcHelp", new ResourceModel("garbageHelp"));
        gcBorder.add(gcHelpBubble);
    }

    private void addArtifactsCleanupMaintenance(Form form) {
        final Border cleanupBorder = new TitledBorder("cleanupBorder",
                new CompoundPropertyModel(cleanupConfigDescriptor));
        form.add(cleanupBorder);
        TextField<String> cronExpTextField = new TextField<>("cronExp");
        cronExpTextField.setRequired(true);
        cronExpTextField.add(CronExpValidator.getInstance());
        cleanupBorder.add(cronExpTextField);
        cleanupBorder.add(new SchemaHelpBubble("cronExp.help"));
        cleanupBorder.add(new CronNextDatePanel("cronNextDatePanel", cronExpTextField));

        TitledAjaxLink collectLink = new TitledAjaxLink("cleanup", "Run Unused Cached Artifacts Cleanup") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                BasicStatusHolder statusHolder = new BasicStatusHolder();
                artifactCleanupService.callManualArtifactCleanup(statusHolder);
                if (statusHolder.isError()) {
                    error("Could not run the artifact cleanup: " + statusHolder.getLastError().getMessage() + ".");
                } else {
                    info("Artifact cleanup was successfully scheduled to run in the background.");
                }
            }
        };
        cleanupBorder.add(collectLink);
        HelpBubble cleanupHelpBubble = new HelpBubble("cleanupHelp", new ResourceModel("cleanupHelp"));
        cleanupBorder.add(cleanupHelpBubble);
    }

    private void addVirtualCleanupMaintenance(Form form) {
        final Border virtualCleanupBorder = new TitledBorder("virtualCleanupBorder",
                new CompoundPropertyModel(virtualCacheCleanupConfigDescriptor));
        form.add(virtualCleanupBorder);
        TextField<String> virtualCronExpTextField = new TextField<>("cronExp");
        virtualCronExpTextField.setRequired(true);
        virtualCronExpTextField.add(CronExpValidator.getInstance());
        virtualCleanupBorder.add(virtualCronExpTextField);
        virtualCleanupBorder.add(new SchemaHelpBubble("virtualCronExp.help", "cronExp"));
        virtualCleanupBorder.add(new CronNextDatePanel("virtualCronNextDatePanel", virtualCronExpTextField));

        TitledAjaxLink collectLink = new TitledAjaxLink("virtualCleanup", "Clean Virtual Repositories Now") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                BasicStatusHolder statusHolder = new BasicStatusHolder();
                virtualCacheCleanupService.callVirtualCacheCleanup(statusHolder);
                if (statusHolder.isError()) {
                    error("Could not run the virtual cache cleanup: " + statusHolder.getLastError().getMessage() + ".");
                } else {
                    info("Virtual cache cleanup was successfully scheduled to run in the background.");
                }
            }
        };
        virtualCleanupBorder.add(collectLink);
        HelpBubble virtualCleanupHelpBubble = new HelpBubble("virtualCleanupHelp", new ResourceModel("virtualCleanupHelp"));
        virtualCleanupBorder.add(virtualCleanupHelpBubble);
    }

    @Override
    public String getPageName() {
        return "Maintenance";
    }
}