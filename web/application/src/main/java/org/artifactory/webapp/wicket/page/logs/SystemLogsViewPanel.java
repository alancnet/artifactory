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

package org.artifactory.webapp.wicket.page.logs;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.component.template.HtmlTemplate;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.artifactory.common.wicket.util.JavaScriptUtils.jsFunctionCall;

/**
 * This panel serves as as auto display of the information and content in the system log file, with an option of
 * downloading the file
 *
 * @author Noam Tenne
 */
public class SystemLogsViewPanel extends Panel {
    private static final int FIRST_READ_BLOCK_SIZE = 100 * 1024;

    private File logDir = ContextHelper.get().getArtifactoryHome().getLogDir();

    /**
     * File object of the system log
     */
    private File systemLogFile = new File(logDir, "artifactory.log");

    /**
     * Link object for downloading the system log file
     */
    private DownloadLink downloadLink;

    /**
     * Label to represent the download link caption
     */
    Label linkLabel = new Label("linkLabel", "Download");

    /**
     * Label to display the size of the system log file
     */
    private Label sizeLabel = new Label("systemLogsSize", "");

    /**
     * Label to display the content of the system log file (100K at a time)
     */
    private Label contentLabel = new Label("systemLogsContent", "");

    /**
     * Label to display file last modified and view last updated times
     */
    private Label lastUpdateLabel = new Label("lastUpdate", "");

    /**
     * Pointer to indicate the last position the log file was read from
     */
    private long lastPointer;

    /**
     * List containing log file names
     */
    private static final List<String> LOGS =
            asList("artifactory.log", "access.log", "import.export.log", "request.log");

    /**
     * Main constructor
     *
     * @param id The verbal ID of the panel
     */
    public SystemLogsViewPanel(String id) {
        super(id);
        addLogComboBox();
        addSystemLogsSize();
        addSystemLogsLink();
        addSystemLogsContent();
        addLastUpdate();

        // add the timer behavior to the page and make it update both components
        add(new AbstractAjaxTimerBehavior(Duration.seconds(ConstantValues.logsViewRefreshRateSecs.getInt())) {
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                updateComponents(target, (!systemLogFile.exists()));
            }
        });
    }

    /**
     * Adds a combo box with log file choices
     */
    private void addLogComboBox() {
        final DropDownChoice logsDropDownChoice =
                new DropDownChoice<String>("logs", Model.of(LOGS.get(0)), LOGS) {

                    @Override
                    protected boolean wantOnSelectionChangedNotifications() {
                        return true;
                    }
                };
        /**
         * Add behavior for when the combo box selection is changed
         */
        logsDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                int choice = Integer.parseInt(logsDropDownChoice.getValue());
                List choices = logsDropDownChoice.getChoices();
                String selectedLog = choices.get(choice).toString();
                systemLogFile = new File(logDir, selectedLog);
                updateComponents(target, true);
            }
        });

        add(logsDropDownChoice);
    }

    /**
     * Update the different display components
     *
     * @param target     The AjaxRequestTarget from our action
     * @param cleanPanel True if the text container should be cleaned of content. false if not
     */
    private void updateComponents(AjaxRequestTarget target, boolean cleanPanel) {
        //Make sure the bottom of the text area will be displayed after every update
        target.appendJavaScript(jsFunctionCall("ArtifactoryLog.log",
                contentLabel.getMarkupId(),
                readLogAndUpdateSize(cleanPanel),
                cleanPanel)
        );
        target.add(downloadLink);
        target.add(sizeLabel);
        target.add(linkLabel);
        target.add(lastUpdateLabel);
    }

    /**
     * Add a label with the size of the system log file
     */
    private void addSystemLogsSize() {
        add(sizeLabel);
        sizeLabel.setOutputMarkupId(true);
    }

    /**
     * Add a link to enable the download of the system log file
     */
    private void addSystemLogsLink() {
        // This is very ugly and should be removed when we upgrade wicket, see RTFACT-5470 for explanation
        downloadLink = new DownloadLink("systemLogsLink", systemLogFile) {
            @Override
            public void onClick() {
                final File file = getModelObject();
                IResourceStream resourceStream = new FileResourceStream(new org.apache.wicket.util.file.File(file));
                ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(resourceStream) {
                    @Override
                    public void respond(IRequestCycle requestCycle) {
                        IResource.Attributes attributes = new IResource.Attributes(requestCycle.getRequest(),
                                requestCycle.getResponse());

                        ResourceStreamResource resource = new ResourceStreamResource(this.getResourceStream()) {
                            @Override
                            protected void configureCache(ResourceResponse data, Attributes attributes) {
                                Response response = attributes.getResponse();
                                ((WebResponse) response).disableCaching();
                            }
                        };
                        resource.setFileName(file.getName());
                        resource.setContentDisposition(ContentDisposition.ATTACHMENT);
                        resource.respond(attributes);
                    }
                };
                getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
            }
        };

        add(downloadLink);
        downloadLink.add(linkLabel);
        downloadLink.setOutputMarkupId(true);
        linkLabel.setOutputMarkupId(true);
    }

    /**
     * Add a label with the log file and view update times
     */
    private void addLastUpdate() {
        add(lastUpdateLabel);
        lastUpdateLabel.setOutputMarkupId(true);
    }

    /**
     * A a label to display the content of the system log file (last 100K)
     */
    private void addSystemLogsContent() {
        add(contentLabel);
        contentLabel.setOutputMarkupId(true);
        contentLabel.setEscapeModelStrings(false);
        contentLabel.setDefaultModelObject(readLogAndUpdateSize(false));

        HtmlTemplate initScript = new HtmlTemplate("initScript");
        initScript.setParameter("logDivId", new PropertyModel(contentLabel, "markupId"));
        add(initScript);
    }

    /**
     * Attemps to continue reading the log file from the last position, and the updates the log path, size and link
     * According to the outcome.
     *
     * @param cleanPanel True if the text container should be cleaned of content. false if not
     * @return String - The newly read content
     */
    protected String readLogAndUpdateSize(boolean cleanPanel) {
        if ((lastPointer > systemLogFile.length()) || cleanPanel) {
            lastPointer = 0;
        }
        long size = systemLogFile.length();
        setLogInfo();
        if (lastPointer == size) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        RandomAccessFile logRandomAccessFile = null;
        try {
            logRandomAccessFile = new RandomAccessFile(systemLogFile, "r");

            //If the log file is larger than 100K
            if (lastPointer == 0 && logRandomAccessFile.length() > FIRST_READ_BLOCK_SIZE) {
                //Point to the begining of the last 100K
                lastPointer = logRandomAccessFile.length() - FIRST_READ_BLOCK_SIZE;
            }
            logRandomAccessFile.seek(lastPointer);

            String line;
            while ((line = logRandomAccessFile.readLine()) != null) {
                CharSequence escapedLine = Strings.escapeMarkup(line, false, false);
                sb.append("<div>").append(escapedLine).append("<br/></div>");
            }
            lastPointer = logRandomAccessFile.getFilePointer();

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                if (logRandomAccessFile != null) {
                    logRandomAccessFile.close();
                }
            } catch (IOException ignore) {
            }
        }
        return sb.toString();
    }

    /**
     * Sets the attributes of the log size, download links and update times according to the log availability
     */
    private void setLogInfo() {
        if (!systemLogFile.exists()) {
            sizeLabel.setDefaultModelObject("");
            linkLabel.setDefaultModelObject("");
            lastUpdateLabel.setDefaultModelObject("");
        } else {
            sizeLabel.setDefaultModelObject("(" + FileUtils.byteCountToDisplaySize(systemLogFile.length()) + ")");
            linkLabel.setDefaultModelObject("Download");
            StringBuilder sb = new StringBuilder();
            Date logLastModified = new Date(systemLogFile.lastModified());
            Date viewLastUpdate = new Date(System.currentTimeMillis());
            sb.append("File last modified: ").append(logLastModified).append(". ");
            sb.append("View last updated: ").append(viewLastUpdate).append(".");
            lastUpdateLabel.setDefaultModelObject(sb.toString());
            downloadLink.setDefaultModel(new Model<>(systemLogFile));
        }
    }
}
