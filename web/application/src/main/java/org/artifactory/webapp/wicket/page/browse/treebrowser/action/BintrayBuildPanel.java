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

package org.artifactory.webapp.wicket.page.browse.treebrowser.action;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.artifactory.api.bintray.BintrayParams;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.common.StatusEntry;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.panel.feedback.UnescapedFeedbackMessage;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.common.wicket.util.WicketUtils;
import org.jfrog.build.api.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Displays a list of Bintray properties and enables the user to push a build to Bintray
 *
 * @author Shay Yaakov
 */
public class BintrayBuildPanel extends BintrayBasePanel {
    private static final Logger log = LoggerFactory.getLogger(BintrayBuildPanel.class);

    private Build build;

    public BintrayBuildPanel(Build build) {
        this.build = build;
        initBintrayModel();
        initComponents();
    }

    @Override
    protected void initBintrayModel() {
        bintrayModel = new BintrayParams();
        setDefaultModel(new CompoundPropertyModel<>(bintrayModel));
    }

    @Override
    protected boolean isFieldRequired() {
        return false;
    }

    @Override
    protected void addExtraComponentsToForm(final Form form) {
        final StyledCheckbox useExistingPropsCheckbox = new StyledCheckbox("useExistingProps");
        useExistingPropsCheckbox.setTitle("Use Bintray-specific artifact properties");
        form.add(useExistingPropsCheckbox);
        form.add(new HelpBubble("useExistingProps.help", new ResourceModel("useExistingProps.help")));

        StyledCheckbox notifyCheckbox = new StyledCheckbox("notify");
        notifyCheckbox.setTitle("Send Email Notification");
        form.add(notifyCheckbox);
        form.add(new HelpBubble("notify.help", new ResourceModel("notify.help")));
    }

    @Override
    protected String getBintrayDescriptionLabel() {
        return "Distribute this build's artifacts to users by uploading them to " + getBintrayLink() + "." +
                "<br/><span style='display:block; font-size:0.85em; line-height:1.3em'>Bintray is a public online service through which you can share your release binaries with the world. " +
                "<br/>Note that once artifacts are pushed, you need to publish them in Bintray in order to make them world-visible.</span>";
    }

    @Override
    protected void addExtraButton(Form form) {
        TitledAjaxSubmitLink backgroundButton = new TitledAjaxSubmitLink("background", "Background Push", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                Map<String, String> headersMap = WicketUtils.getHeadersMap();
                bintrayService.executeAsyncPushBuild(build, bintrayModel, headersMap);
                String buildNameAndNumber = build.getName() + ":" + build.getNumber();
                String message = String.format(
                        "Background Push of build '%s' to Bintray successfully scheduled to run.", buildNameAndNumber);
                log.info(message);
                getPage().info(message);
                AjaxUtils.refreshFeedback(target);
                ModalHandler.closeCurrent(target);
            }
        };
        add(backgroundButton);
    }

    @Override
    protected void onPushClicked() {
        try {
            Map<String, String> headersMap = WicketUtils.getHeadersMap();
            BasicStatusHolder statusHolder = bintrayService.pushBuild(build, bintrayModel, headersMap);
            if (statusHolder.hasErrors()) {
                getPage().error(statusHolder.getLastError().getMessage());
            } else if (statusHolder.hasWarnings()) {
                List<StatusEntry> warnings = statusHolder.getWarnings();
                getPage().warn(warnings.get(warnings.size() - 1).getMessage());
            } else {
                StringBuilder successMessagesBuilder = new StringBuilder();
                String buildNameAndNumber = build.getName() + ":" + build.getNumber();
                successMessagesBuilder.append("Successfully pushed build '").append(buildNameAndNumber).append("' to ");
                String versionFilesPathUrl = bintrayService.getVersionFilesUrl(bintrayModel);
                successMessagesBuilder.append("<a href=\"").append(versionFilesPathUrl).append("\" target=\"_blank\">")
                        .append(versionFilesPathUrl).append("</a>.");
                getPage().info(new UnescapedFeedbackMessage(successMessagesBuilder.toString()));
            }
        } catch (IOException e) {
            if (getFeedbackMessages().isEmpty()) {
                getPage().error("Connection failed with exception: " + e.getMessage());
            }
        }
    }
}
