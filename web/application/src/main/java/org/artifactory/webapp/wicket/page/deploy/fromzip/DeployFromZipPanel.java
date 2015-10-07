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

package org.artifactory.webapp.wicket.page.deploy.fromzip;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.repo.DeployService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.StatusEntry;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.panel.feedback.UnescapedFeedbackMessage;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.common.wicket.panel.upload.FileUploadForm;
import org.artifactory.common.wicket.panel.upload.UploadListener;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.descriptor.repo.LocalRepoAlphaComparator;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.webapp.wicket.page.logs.SystemLogsPage;
import org.artifactory.webapp.wicket.panel.upload.DefaultFileUploadForm;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Gives the user an interface for deployment of artifacts which are stored in an archive
 *
 * @author Noam Tenne
 */
public class DeployFromZipPanel extends TitledPanel implements UploadListener {

    @SpringBean
    private AuthorizationService authorizationService;

    @SpringBean
    private RepositoryService repositoryService;

    @SpringBean
    private DeployService deployService;

    @SpringBean
    private SecurityService securityService;

    private DefaultFileUploadForm deployForm;

    @WicketProperty
    private LocalRepoDescriptor targetRepo;

    public DeployFromZipPanel(String id) {
        super(id);

        //Add upload form with ajax progress bar
        deployForm = new DefaultFileUploadForm("deployForm", this);

        TitledBorder uploadBorder = new TitledBorder("uploadBorder") {
            @Override
            public boolean isVisible() {
                return super.isVisible() && deployForm.isVisible();
            }
        };
        add(uploadBorder);
        uploadBorder.add(deployForm);

        PropertyModel<LocalRepoDescriptor> targetRepoModel = new PropertyModel<>(this, "targetRepo");
        List<LocalRepoDescriptor> deployableRepos = getDeployableRepos();
        DropDownChoice targetRepo = new DropDownChoice<>(
                "targetRepo", targetRepoModel, deployableRepos);
        if (deployableRepos.size() > 0) {
            LocalRepoDescriptor defaultTarget = deployableRepos.get(0);
            targetRepo.setDefaultModelObject(defaultTarget);
        } //Else - BUG!
        deployForm.add(targetRepo);

        deployForm.add(new HelpBubble("deployHelp", getDeployHelp()));
        deployForm.add(new HelpBubble("supportedArchiveExtensions.help",
                "The following archive extensions are supported: zip, tar, tar.gz, tgz"));
    }

    /**
     * Returns the content string of the archive selection help bubble
     *
     * @return String - Text for archive selection help buble
     */
    private String getDeployHelp() {
        return "When deploying an artifacts bundle, the file structure within the archive you select is maintained.";
    }

    @Override
    public void info(String message) {
        super.info(message);
    }

    @Override
    public void onException() {
    }

    /**
     * Remove uploaded file from the form
     */
    public void removeUploadedFile() {
        if (deployForm != null) {
            deployForm.removeUploadedFile();
        }
    }

    /**
     * Executes when the uploaded file is saved
     *
     * @param file
     */
    @Override
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public void onFileSaved(File file) {
        File uploadedFile = deployForm.getUploadedFile();
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        try {
            deployService.deployBundle(uploadedFile, targetRepo, statusHolder, false);
            List<StatusEntry> errors = statusHolder.getErrors();
            List<StatusEntry> warnings = statusHolder.getWarnings();

            String logs;
            if (authorizationService.isAdmin()) {
                String systemLogsPage = WicketUtils.absoluteMountPathForPage(SystemLogsPage.class);
                logs = "<a href=\"" + systemLogsPage + "\">log</a>";
            } else {
                logs = "log";
            }

            if (!errors.isEmpty()) {
                error(new UnescapedFeedbackMessage(
                        "There were " + errors.size() + " errors during deployment. Please review the " + logs +
                                " for more details."));
            } else if (!warnings.isEmpty()) {
                warn(new UnescapedFeedbackMessage(
                        "There were " + warnings.size() + " warnings during deployment. Please review the " + logs +
                                " for more details."));
            } else {
                info(statusHolder.getStatusMsg());
            }
        } finally {
            //Delete the uploaded file
            FileUtils.deleteQuietly(uploadedFile);
        }
    }

    /**
     * Returns the uploaded file from the form
     *
     * @return File - The uploaded file from the form
     */
    public File getUploadedFile() {
        return this.deployForm.getUploadedFile();
    }

    /**
     * Returns the upload form
     *
     * @return FileUploadForm - The used upload form
     */
    public FileUploadForm getUploadForm() {
        return deployForm;
    }

    /**
     * Returns all the deployable repos from the repository service
     *
     * @return List - All deployable repos
     */
    private List<LocalRepoDescriptor> getDeployableRepos() {
        List<LocalRepoDescriptor> repos = repositoryService.getDeployableRepoDescriptors();
        Collections.sort(repos, new LocalRepoAlphaComparator());
        return repos;
    }
}
