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

package org.artifactory.webapp.wicket.page.importexport.system;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.artifactory.api.common.ImportExportStatusHolder;
import org.artifactory.api.config.ExportSettingsImpl;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.StatusEntry;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.file.browser.button.FileBrowserButton;
import org.artifactory.common.wicket.component.file.path.PathAutoCompleteTextField;
import org.artifactory.common.wicket.component.file.path.PathMask;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.panel.feedback.UnescapedFeedbackMessage;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.webapp.wicket.page.logs.SystemLogsPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yoav Landman
 */
public class ExportSystemPanel extends TitledPanel {
    private static final Logger log = LoggerFactory.getLogger(ExportSystemPanel.class);

    @WicketProperty
    private File exportToPath;

    @WicketProperty
    private boolean createArchive;

    @WicketProperty
    private boolean m2Compatible;

    @WicketProperty
    private boolean excludeMetadata;

    @WicketProperty
    private boolean excludeBuilds;

    @WicketProperty
    private boolean excludeContent;

    @WicketProperty
    private boolean verbose;

    @SpringBean
    private RepositoryService repositoryService;

    public ExportSystemPanel(String string) {
        super(string);
        Form exportForm = new SecureForm("exportForm");
        add(exportForm);

        PropertyModel pathModel = new PropertyModel(this, "exportToPath");
        final PathAutoCompleteTextField exportToPathTf = new PathAutoCompleteTextField("exportToPath", pathModel);
        exportToPathTf.setMask(PathMask.FOLDERS);
        exportToPathTf.setRequired(true);
        exportForm.add(exportToPathTf);

        FileBrowserButton browserButton = new FileBrowserButton("fileBrowser", pathModel) {
            @Override
            protected void onOkClicked(AjaxRequestTarget target) {
                super.onOkClicked(target);
                target.add(exportToPathTf);
            }
        };
        browserButton.setMask(PathMask.FOLDERS);
        exportForm.add(browserButton);

        final StyledCheckbox m2CompatibleCheckbox =
                new StyledCheckbox("m2Compatible", new PropertyModel(this, "m2Compatible"));
        m2CompatibleCheckbox.setOutputMarkupId(true);
        exportForm.add(m2CompatibleCheckbox);
        exportForm.add(new HelpBubble("m2CompatibleHelp",
                "Mark to include Maven 2 repository metadata and checksum files as part of the export"));

        final StyledCheckbox excludeMetadataCheckbox =
                new StyledCheckbox("excludeMetadata", new PropertyModel(this, "excludeMetadata"));
        excludeMetadataCheckbox.setOutputMarkupId(true);
        exportForm.add(excludeMetadataCheckbox);
        exportForm.add(new HelpBubble("excludeMetadataHelp",
                "Mark to exclude repositories metadata from the export.\n" +
                        "(Maven 2 metadata is unaffected by this setting)"));

        final StyledCheckbox excludeBuildsCheckbox =
                new StyledCheckbox("excludeBuilds", new PropertyModel(this, "excludeBuilds"));
        excludeBuildsCheckbox.setOutputMarkupId(true);
        exportForm.add(excludeBuildsCheckbox);
        exportForm.add(new HelpBubble("excludeBuildsHelp", "Mark to exclude all builds from the export."));

        final StyledCheckbox excludeContentCheckbox =
                new StyledCheckbox("excludeContent", new PropertyModel(this, "excludeContent"));
        excludeContentCheckbox.setOutputMarkupId(true);
        excludeContentCheckbox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                boolean excludeMDSelected = excludeMetadataCheckbox.isChecked();
                boolean excludeBuildsSelected = excludeBuildsCheckbox.isChecked();
                boolean excludeContentSelected = excludeContentCheckbox.isChecked();
                if (excludeMDSelected != excludeContentSelected) {
                    excludeMetadataCheckbox.setDefaultModelObject(excludeContentSelected);
                }
                if (excludeBuildsSelected != excludeContentSelected) {
                    excludeBuildsCheckbox.setDefaultModelObject(excludeContentSelected);
                }

                m2CompatibleCheckbox.setEnabled(!excludeContentSelected);

                target.add(excludeMetadataCheckbox);
                target.add(m2CompatibleCheckbox);
                target.add(excludeBuildsCheckbox);
            }
        });
        exportForm.add(excludeContentCheckbox);
        exportForm.add(new HelpBubble("excludeContentHelp",
                "Exclude repository binaries from the export."));

        //Create a zip archive (slow!)
        exportForm.add(new StyledCheckbox("createArchive", new PropertyModel<Boolean>(this, "createArchive")));

        StyledCheckbox verboseCheckbox = new StyledCheckbox("verbose", new PropertyModel<Boolean>(this, "verbose"));
        verboseCheckbox.setRequired(false);
        exportForm.add(verboseCheckbox);
        String systemLogsPage = WicketUtils.absoluteMountPathForPage(SystemLogsPage.class);
        exportForm.add(
                new HelpBubble("verboseHelp", "Lowers the log level to debug and redirects the output from the " +
                        "standard log to the import-export log." + "\nHint: You can monitor the log in the <a href=\"" +
                        systemLogsPage + "\">'System Logs'</a> page."));

        final ImportExportStatusHolder status = new ImportExportStatusHolder();
        TitledAjaxSubmitLink exportButton = new TitledAjaxSubmitLink("export", "Export", exportForm) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                ArtifactoryContext context = ContextHelper.get();
                try {
                    Session.get().cleanupFeedbackMessages();
                    status.reset();
                    ExportSettings settings = new ExportSettingsImpl(exportToPath, status);
                    settings.setCreateArchive(createArchive);
                    settings.setFailFast(false);
                    settings.setVerbose(verbose);
                    settings.setFailIfEmpty(true);
                    settings.setIncludeMetadata(!excludeMetadata);
                    settings.setExcludeBuilds(excludeBuilds);
                    settings.setM2Compatible(m2Compatible);
                    settings.setExcludeContent(excludeContent);
                    if (!excludeMetadata || !excludeContent) {
                        settings.setRepositories(getAllLocalRepoKeys());
                    }
                    context.exportTo(settings);
                    List<StatusEntry> warnings = status.getWarnings();
                    if (!warnings.isEmpty()) {
                        String systemLogsPage = WicketUtils.absoluteMountPathForPage(SystemLogsPage.class);
                        warn(new UnescapedFeedbackMessage(
                                warnings.size() + " warning(s) reported during the export. Please review the " +
                                        "<a href=\"" + systemLogsPage + "\">log</a> for further information."));
                    }
                    if (status.isError()) {
                        String message = status.getStatusMsg();
                        Throwable exception = status.getException();
                        if (exception != null) {
                            message = exception.getMessage();
                        }
                        error("Failed to export system to '" + exportToPath + "': " + message);
                    } else {
                        File exportFile = settings.getOutputFile();
                        info("Successfully exported system to '" + exportFile.getPath() + "'.");
                    }
                } catch (Exception e) {
                    error("Failed to export system to '" + exportToPath + "': " + e.getMessage());
                    log.error("Failed to export system.", e);
                }
                AjaxUtils.refreshFeedback(target);
            }
        };
        exportForm.add(exportButton);
        exportForm.add(new DefaultButtonBehavior(exportButton));

        final Label statusLabel = new Label("status");
        statusLabel.add(new AjaxSelfUpdatingTimerBehavior(Duration.milliseconds(1000)) {
            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                super.onPostProcessTarget(target);
                statusLabel.setDefaultModel(new PropertyModel(status, "status"));
            }
        });
    }

    private List<String> getAllLocalRepoKeys() {
        List<String> repoKeys = new ArrayList<>();
        for (LocalRepoDescriptor localRepoDescriptor : repositoryService.getLocalAndCachedRepoDescriptors()) {
            repoKeys.add(localRepoDescriptor.getKey());
        }
        return repoKeys;
    }
}