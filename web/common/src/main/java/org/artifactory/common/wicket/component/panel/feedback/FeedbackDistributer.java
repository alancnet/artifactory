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

package org.artifactory.common.wicket.component.panel.feedback;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessagesModel;
import org.apache.wicket.feedback.IFeedback;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.util.string.Strings;
import org.artifactory.common.wicket.contributor.ResourcePackage;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author Yoav Aharoni
 */
@SuppressWarnings({"BooleanMethodNameMustStartWithQuestion"})
public class FeedbackDistributer extends Panel implements IFeedback {
    private FeedbackMessagesPanel defaultFeedbackPanel;

    public FeedbackDistributer(String id) {
        super(id);

        setDefaultModel(newFeedbackMessagesModel());
        setOutputMarkupId(true);

        add(ResourcePackage.forJavaScript(FeedbackDistributer.class));

        Label script = new Label("script", new ScriptModel());
        script.setEscapeModelStrings(false);
        script.setVersioned(false);
        add(script);
    }

    public FeedbackMessagesPanel getDefaultFeedbackPanel() {
        return defaultFeedbackPanel;
    }

    public void setDefaultFeedbackPanel(FeedbackMessagesPanel defaultFeedbackPanel) {
        this.defaultFeedbackPanel = defaultFeedbackPanel;
    }

    /**
     * @see Component#isVersioned()
     */
    @SuppressWarnings({"RefusedBequest"})
    @Override
    public boolean isVersioned() {
        return false;
    }

    public final FeedbackMessagesModel getFeedbackMessagesModel() {
        return (FeedbackMessagesModel) getDefaultModel();
    }

    public void updateFeedback() {
        // Force model to load
        getDefaultModelObject();
    }

    public boolean anyErrorMessage() {
        return anyMessage(FeedbackMessage.ERROR);
    }

    public boolean anyMessage() {
        return anyMessage(FeedbackMessage.UNDEFINED);
    }

    public boolean anyMessage(int level) {
        List<FeedbackMessage> msgs = getCurrentMessages();

        for (FeedbackMessage msg : msgs) {
            if (msg.isLevel(level)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings({"unchecked"})
    protected final List<FeedbackMessage> getCurrentMessages() {
        List<FeedbackMessage> messages = (List<FeedbackMessage>) getDefaultModelObject();
        return Collections.unmodifiableList(messages);
    }

    protected FeedbackMessagesModel newFeedbackMessagesModel() {
        return new FeedbackMessagesModel(this);
    }

    private class ScriptModel extends AbstractReadOnlyModel {
        @Override
        public Object getObject() {
            StringBuffer initScript = new StringBuffer();

            // init
            String defaultFeedbackId = defaultFeedbackPanel == null
                    ? "" : defaultFeedbackPanel.getMarkupId();

            initScript
                    .append("(function(fd) {\n")
                    .append("fd.init('")
                    .append(defaultFeedbackId)
                    .append("');\n");

            // add messages
            for (FeedbackMessage feedbackMessage : getCurrentMessages()) {
                feedbackMessage.markRendered();
                Serializable message = feedbackMessage.getMessage();

                String messageString = (message instanceof UnescapedFeedbackMessage) ?
                        message.toString() : Strings.escapeMarkup(message.toString(), false, false).toString();
                initScript
                        .append("fd.addMessage('")
                        .append(getMarkupIdFor(feedbackMessage.getReporter()))
                        .append("', '")
                        .append(feedbackMessage.getLevelAsString())
                        .append("', ")
                        .append(asJsStringParam(messageString))
                        .append(");\n");
            }

            initScript.append("fd.showMessages();\n");
            initScript.append("})(FeedbackDistributer);\n");
            return initScript;
        }

        private String getMarkupIdFor(Component component) {
            Component current = component;

            // find first parent with markup id
            while (current != null) {
                if (current.getOutputMarkupId() &&
                        (!current.getRenderBodyOnly() || current.getOutputMarkupPlaceholderTag())) {
                    return current.getMarkupId();
                }

                current = current.getParent();
            }

            return "";
        }
    }

    public static String asJsStringParam(Object param) {
        return '\'' + param.toString()
                .replaceAll("\\\\", "\\\\\\\\")
                .replaceAll("'", "\\\\'")
                .replaceAll("\n", "<br/>")
                .replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                .replaceAll("\r", "") + '\'';
    }
}
