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

import org.apache.commons.io.FileUtils;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;
import org.artifactory.api.common.ImportExportStatusHolder;
import org.artifactory.api.config.ImportSettingsImpl;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.StatusEntry;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.ajax.ConfirmationAjaxCallDecorator;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.file.browser.button.FileBrowserButton;
import org.artifactory.common.wicket.component.file.path.PathAutoCompleteTextField;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.panel.feedback.UnescapedFeedbackMessage;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.util.ZipUtils;
import org.artifactory.webapp.wicket.application.ArtifactoryApplication;
import org.artifactory.webapp.wicket.page.logs.SystemLogsPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * @author Yoav Landman
 */
public class ImportSystemPanel extends TitledPanel {
    private static final Logger log = LoggerFactory.getLogger(ImportSystemPanel.class);

    @WicketProperty
    private File importFromPath;

    @WicketProperty
    private boolean verbose;

    @WicketProperty
    private boolean excludeMetadata;

    @WicketProperty
    private boolean excludeContent;

    public ImportSystemPanel(String string) {
        super(string);

        Form importForm = new SecureForm("importForm");
        add(importForm);
        PropertyModel<File> pathModel = new PropertyModel<>(this, "importFromPath");
        final PathAutoCompleteTextField importToPathTf =
                new PathAutoCompleteTextField("importFromPath", pathModel);
        importToPathTf.setRequired(true);
        importForm.add(importToPathTf);

        importForm.add(new FileBrowserButton("browseButton", pathModel) {
            @Override
            protected void onOkClicked(AjaxRequestTarget target) {
                super.onOkClicked(target);
                target.add(importToPathTf);
            }
        });

        addExcludeMetadataCheckbox(importForm);
        addVerboseCheckbox(importForm);

        addImportButton(importForm);
    }

    private void addVerboseCheckbox(Form importForm) {
        StyledCheckbox verboseCheckbox = new StyledCheckbox("verbose", new PropertyModel<Boolean>(this, "verbose"));
        verboseCheckbox.setRequired(false);
        importForm.add(verboseCheckbox);
        String systemLogsPage = WicketUtils.absoluteMountPathForPage(SystemLogsPage.class);
        importForm.add(
                new HelpBubble("verboseHelp", "Lowers the log level to debug and redirects the output from the " +
                        "standard log to the import-export log." +
                        "\nHint: You can monitor the log in the <a href=\"" + systemLogsPage +
                        "\">'System Logs'</a> page."));
    }

    private void addExcludeMetadataCheckbox(Form importForm) {
        final StyledCheckbox excludeMetadataCheckbox =
                new StyledCheckbox("excludeMetadata", new PropertyModel<Boolean>(this, "excludeMetadata"));
        excludeMetadataCheckbox.setOutputMarkupId(true);
        importForm.add(excludeMetadataCheckbox);
        importForm.add(new HelpBubble("excludeMetadataHelp",
                "Mark to exclude repositories metadata from the import."));

        final StyledCheckbox excludeContentCheckbox =
                new StyledCheckbox("excludeContent", new PropertyModel<Boolean>(this, "excludeContent"));
        excludeContentCheckbox.setOutputMarkupId(true);
        excludeContentCheckbox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                boolean excludeMDSelected = excludeMetadataCheckbox.isChecked();
                boolean excludeContentSelected = excludeContentCheckbox.isChecked();
                if (excludeMDSelected != excludeContentSelected) {
                    excludeMetadataCheckbox.setDefaultModelObject(excludeContentSelected);
                    target.add(excludeMetadataCheckbox);
                }
            }
        });
        importForm.add(excludeContentCheckbox);
        importForm.add(new HelpBubble("excludeContentHelp",
                "Mark to exclude repository binaries from the import.\n"));
    }

    @SuppressWarnings({"unchecked"})
    private void addImportButton(final Form importForm) {
        TitledAjaxSubmitLink importButton = new TitledAjaxSubmitLink("import", "Import", importForm) {
            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                String confirmImportMessage =
                        "Full system import deletes all existing Artifactory content.\n" +
                                "Are you sure you want to continue?";

                return new ConfirmationAjaxCallDecorator(super.getAjaxCallDecorator(),
                        confirmImportMessage);
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                Session.get().cleanupFeedbackMessages();
                ImportExportStatusHolder status = new ImportExportStatusHolder();
                //If the path denotes an archive extract it first, else use the directory
                File importFromFolder = null;
                try {
                    if (!importFromPath.exists()) {
                        error("Specified location '" + importFromPath +
                                "' does not exist.");
                        return;
                    }
                    if (importFromPath.isDirectory()) {
                        if (importFromPath.list().length == 0) {
                            error("Directory '" + importFromPath + "' is empty.");
                            return;
                        }
                        importFromFolder = importFromPath;

                    } else if (isZip(importFromPath)) {
                        //Extract the archive
                        status.status("Extracting archive...", log);
                        ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
                        importFromFolder =
                                new File(artifactoryHome.getTempUploadDir(),
                                        importFromPath.getName() + "_extract");
                        FileUtils.deleteDirectory(importFromFolder);
                        FileUtils.forceMkdir(importFromFolder);
                        try {
                            ZipUtils.extract(importFromPath, importFromFolder);
                        } catch (Exception e) {
                            String message = "Failed to extract file " + importFromPath.getAbsolutePath();
                            error(message);
                            log.error(message, e);
                            return;
                        }
                    } else {
                        error("Failed to import system from '" + importFromPath +
                                "': Unrecognized file type.");
                        return;
                    }
                    status.status("Importing from directory...", log);
                    ArtifactoryContext context = ContextHelper.get();
                    ImportSettingsImpl importSettings = new ImportSettingsImpl(importFromFolder, status);
                    importSettings.setFailFast(false);
                    importSettings.setFailIfEmpty(true);
                    importSettings.setVerbose(verbose);
                    importSettings.setIncludeMetadata(!excludeMetadata);
                    importSettings.setExcludeContent(excludeContent);
                    context.importFrom(importSettings);
                    List<StatusEntry> warnings = status.getWarnings();

                    if (!warnings.isEmpty()) {
                        String systemLogsPage = WicketUtils.absoluteMountPathForPage(SystemLogsPage.class);
                        Session.get().warn(new UnescapedFeedbackMessage(
                                warnings.size() + " Warnings have been produces during the export. Please " +
                                        "review the <a href=\"" + systemLogsPage +
                                        "\">log</a> for further information."));
                    }
                    if (status.isError()) {
                        int errorCount = status.getErrors().size();
                        if (errorCount > 1) {
                            String msg = errorCount + " errors occurred while importing system from '" + importFromPath + "': For more accurate information, please look at the log.";
                            Session.get().error(msg);
                        } else {
                            String msg = "Error while importing system from '" + importFromPath + "': " + status.getStatusMsg();
                            Session.get().error(msg);
                        }

                    } else {
                        Session.get().info("Successfully imported system from '" + importFromPath + "'.");
                    }
                    // rebuild site map to open/close pages
                    ArtifactoryApplication.get().rebuildSiteMap();
                    // update logo last modified time
                    ArtifactoryApplication.get().updateLogo();
                    setResponsePage(new ImportExportSystemPage());
                } catch (Exception e) {
                    error("Failed to import system from '" + importFromPath + "': " +
                            e.getMessage());
                    log.error("Failed to import system.", e);
                } finally {
                    if (isZip(importFromPath)) {
                        //Delete the extracted dir
                        try {
                            if (importFromFolder != null) {
                                FileUtils.deleteDirectory(importFromFolder);
                            }
                        } catch (IOException e) {
                            log.warn("Failed to delete export directory: " +
                                    importFromFolder, e);
                        }
                    }
                    status.reset();
                }
            }
        };
        importForm.add(importButton);
        importForm.add(new DefaultButtonBehavior(importButton));
    }

    private boolean isZip(File file) {
        return file.isFile() && file.getName().toLowerCase(Locale.ENGLISH).endsWith(".zip");
    }
}