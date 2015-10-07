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

package org.artifactory.webapp.wicket.page.importexport.repos;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.panel.feedback.UnescapedFeedbackMessage;
import org.artifactory.common.wicket.panel.upload.UploadListener;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.traffic.TrafficService;
import org.artifactory.traffic.entry.UploadEntry;
import org.artifactory.util.HttpUtils;
import org.artifactory.webapp.wicket.page.logs.SystemLogsPage;
import org.artifactory.webapp.wicket.panel.upload.DefaultFileUploadForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Yoav Aharoni
 */
public class ImportZipPanel extends BasicImportPanel implements UploadListener {
    private static final Logger log = LoggerFactory.getLogger(ImportZipPanel.class);

    @SpringBean
    private TrafficService trafficService;

    private DefaultFileUploadForm uploadForm;
    private static final int DEFAULT_BUFF_SIZE = 8192;

    public ImportZipPanel(String string) {
        super(string);

        //Add upload form with ajax progress bar
        uploadForm = new DefaultFileUploadForm("uploadForm", this);
        uploadForm.add(new HelpBubble("uploadHelp", getUploadHelpText()));
        addVerboseCheckbox(uploadForm);
        add(uploadForm);

        getImportForm().setVisible(false);
        getImportForm().add(new HelpBubble("repoSelectHelp", getRepoSelectHelpText()));
    }

    private String getUploadHelpText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Uploads the archive you select.\n");
        sb.append(
                "When importing a single repository, the file structure within the archive should be similar to:\n");
        sb.append("ARCHIVE.ZIP\n");
        sb.append(" |\n");
        sb.append(" |--LIB_DIR_1\n");
        sb.append("\n");
        sb.append(
                "But when importing all repositories, the file structure within the archive should be similar to:\n");
        sb.append("ARCHIVE.ZIP\n");
        sb.append(" |\n");
        sb.append(" |--REPOSITORY_NAME_DIR_1\n");
        sb.append(" |    |\n");
        sb.append(" |    |--LIB_DIR_1\n");
        sb.append("\n");
        sb.append(
                "When importing all repositories, ensure the names of the directories representing\n");
        sb.append(
                "the repositories in the archive, match the names of the target repositories in Artifactory.\n");
        sb.append("NOTE! that uploading the archive, does not import its content.\n");
        sb.append(
                "To import, choose a repository (or all of them) and click Import (appears after upload).\n");
        return sb.toString();
    }

    @Override
    public void info(String message) {
        super.info(message);
    }

    private String getRepoSelectHelpText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Selects where to import the uploaded content.\n");
        sb.append(
                "If the archive contains only artifact libraries, select the repository you would like to import them to.\n");
        sb.append(
                "In a case where the archive contains a structure similar to that of a collection of repositories,\n");
        sb.append("Select \"All Repositories\".");
        return sb.toString();
    }

    @Override
    protected void refreshImportPanel(Form form, AjaxRequestTarget target) {
        form.setVisible(false);
        super.refreshImportPanel(form, target);
    }

    @Override
    public void onException() {
        getImportForm().setVisible(false);
    }

    @Override
    public void onFileSaved(File file) {
        ZipInputStream zipinputstream = null;
        FileOutputStream fos = null;
        File uploadedFile = null;
        File destFolder;
        try {
            uploadedFile = uploadForm.getUploadedFile();
            zipinputstream = new ZipInputStream(new FileInputStream(uploadedFile));
            ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
            destFolder = new File(artifactoryHome.getTempUploadDir(), uploadedFile.getName() + "_extract");
            FileUtils.deleteDirectory(destFolder);

            byte[] buf = new byte[DEFAULT_BUFF_SIZE];
            ZipEntry zipentry;

            zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) {
                //for each entry to be extracted
                String entryName = zipentry.getName();
                File destFile = new File(destFolder, entryName);

                if (zipentry.isDirectory()) {
                    if (!destFile.exists()) {
                        if (!destFile.mkdirs()) {
                            error("Cannot create directory " + destFolder);
                            return;
                        }
                    }
                } else {
                    fos = new FileOutputStream(destFile);
                    int n;
                    while ((n = zipinputstream.read(buf, 0, DEFAULT_BUFF_SIZE)) > -1) {
                        fos.write(buf, 0, n);
                    }
                    fos.close();
                }
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();
            }//while

            setImportFromPath(destFolder);
            getImportForm().setVisible(true);
        } catch (Exception e) {
            String errorMessage = "Error during import of " + uploadedFile;
            String systemLogsPage = WicketUtils.absoluteMountPathForPage(SystemLogsPage.class);
            String logs = ". Please review the <a href=\"" + systemLogsPage + "\">log</a> for further information.";
            error(new UnescapedFeedbackMessage(errorMessage + logs));
            log.error(errorMessage, e);
            onException();
        } finally {
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(zipinputstream);
            FileUtils.deleteQuietly(uploadedFile);
        }
    }

    @Override
    protected void onBeforeImport() {
        File uploadedFile = uploadForm.getUploadedFile();

        //Report the uploaded file to the traffic logger
        if ((uploadedFile != null) && (uploadedFile.exists())) {
            RepoPath repoPath = InternalRepoPathFactory.create(getTargetRepoKey(), "");
            String remoteAddress = HttpUtils.getRemoteClientAddress();
            UploadEntry uploadEntry = new UploadEntry(repoPath.getId(), uploadedFile.length(), 0, remoteAddress);
            trafficService.handleTrafficEntry(uploadEntry);
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        //Cleanup resources if we are not staying on the same page
        Page targetPage = WicketUtils.getPage();
        Page page = getPage();
        if (targetPage != null && !page.equals(targetPage)) {
            cleanupResources();
        }
    }

    @Override
    protected void cleanupResources() {
        log.debug("Cleaning up zip import resources.");
        if (getImportFromPath() != null) {
            try {
                FileUtils.deleteDirectory(getImportFromPath());
            } catch (IOException e) {
                log.warn("Cannot clean extract directory " + getImportFromPath());
            }
            setImportFromPath(null);
        }
        uploadForm.removeUploadedFile();
    }
}