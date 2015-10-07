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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.panel.feedback.UnescapedFeedbackMessage;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.fs.ItemInfo;

import java.io.IOException;
import java.util.Map;

/**
 * Displays a list of Bintray properties and enables the user to push an artifact to Bintray
 *
 * @author Shay Yaakov
 */
public class BintrayArtifactPanel extends BintrayBasePanel {

    private ItemInfo pathToPush;

    public BintrayArtifactPanel(ItemInfo pathToPush) {
        this.pathToPush = pathToPush;
        initBintrayModel();
        initComponents();
    }

    @Override
    protected void initBintrayModel() {
        bintrayModel = bintrayService.createParamsFromProperties(pathToPush.getRepoPath());

        if (StringUtils.isBlank(bintrayModel.getPath())) {
            bintrayModel.setPath(pathToPush.getRelPath());
        }

        if (StringUtils.isBlank(bintrayModel.getPackageId())) {
            ModuleInfo moduleInfo = repoService.getItemModuleInfo(pathToPush.getRepoPath());
            if (moduleInfo.isValid()) {
                bintrayModel.setPackageId(moduleInfo.getPrettyModuleId());
                bintrayModel.setVersion(moduleInfo.getBaseRevision());
            }
        }

        setDefaultModel(new CompoundPropertyModel<>(bintrayModel));
    }

    @Override
    protected boolean isFieldRequired() {
        return true;
    }

    @Override
    protected void addExtraComponentsToForm(Form form) {
        FormComponent<String> relativePathTextField = new TextField<>("path");
        relativePathTextField.setRequired(true);
        form.add(relativePathTextField);
        form.add(new HelpBubble("path.help", new ResourceModel("path.help")));
    }

    @Override
    protected String getBintrayDescriptionLabel() {
        return "Distribute this artifact to users by uploading it to " + getBintrayLink() + "." +
                "<br/><span style='display:block; font-size:0.85em; line-height:1.3em'>Bintray is a public online service through which you can share your release binaries with the world. " +
                "<br/>Note that once artifacts are pushed, you need to publish them in Bintray in order to make them world-visible.</span>";
    }

    @Override
    protected void onPushClicked() {
        try {
            Map<String, String> headersMap = WicketUtils.getHeadersMap();
            BasicStatusHolder statusHolder = bintrayService.pushArtifact(pathToPush, bintrayModel, headersMap);
            if (statusHolder.hasErrors()) {
                getPage().error(statusHolder.getLastError().getMessage());
            } else if (statusHolder.getWarnings().size() != 0) {
                getPage().warn(statusHolder.getWarnings().get(0).getMessage());
            } else {
                StringBuilder successMessagesBuilder = new StringBuilder();
                successMessagesBuilder.append("Successfully pushed '").append(pathToPush.getRelPath()).append("' to ");

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
