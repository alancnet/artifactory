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

package org.artifactory.webapp.wicket.page.home.news;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.message.ArtifactoryUpdatesService;
import org.artifactory.api.message.Message;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.ajax.NoAjaxIndicatorDecorator;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.template.HtmlTemplate;
import org.artifactory.common.wicket.contributor.ResourcePackage;
import org.artifactory.common.wicket.util.CookieUtils;
import org.artifactory.descriptor.config.CentralConfigDescriptor;

/**
 * @author Yoav Aharoni
 */
public class ArtifactoryUpdatesPanel extends Panel {
    private static final String READ_COOKIE_NAME = "new-r";
    private static final String HIDE_COOKIE_NAME = "new-h";
    @SpringBean
    private ArtifactoryUpdatesService artifactoryUpdatesService;
    @SpringBean
    private CentralConfigService centralConfigService;

    public ArtifactoryUpdatesPanel(String id) {
        super(id);
        setOutputMarkupPlaceholderTag(true);

        CentralConfigDescriptor configDescriptor = centralConfigService.getDescriptor();
        if (ConstantValues.versionQueryEnabled.getBoolean() && configDescriptor.isOfflineMode()) {
            setVisible(false);
            return;
        }

        // init message model
        final Message message = artifactoryUpdatesService.getMessage();

        // add collapsible container
        final HtmlTemplate template = new HtmlTemplate("container");
        template.setOutputMarkupId(true);
        template.setVisible(false);
        add(template);

        if (message == null || message == ArtifactoryUpdatesService.PROCESSING_MESSAGE) {
            // no cached message, retry again in 5 secs
            scheduleMessageFetch();
        } else {
            setupMessage(message);
        }
    }

    private void scheduleMessageFetch() {
        add(new AbstractAjaxTimerBehavior(Duration.seconds(5)) {
            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new NoAjaxIndicatorDecorator();
            }

            @Override
            protected void onTimer(AjaxRequestTarget target) {
                stop();
                final Message message = artifactoryUpdatesService.getCachedMessage();
                if (message != null && message != ArtifactoryUpdatesService.PROCESSING_MESSAGE) {
                    setupMessage(message);
                    target.add(ArtifactoryUpdatesPanel.this);
                }
            }
        });
    }

    private void setupMessage(Message message) {
        if (StringUtils.isBlank(message.getBody())) {
            // don't show empty message
            return;
        }

        if (message.getId().equals(CookieUtils.getCookie(HIDE_COOKIE_NAME))) {
            // cookie is set, hide message
            return;
        }

        final HtmlTemplate template = (HtmlTemplate) get("container");
        template.setParameter("markupId", template.getMarkupId());
        template.setParameter("body", message.getBody());
        template.setParameter("cookie", message.getId());
        template.setVisible(true);
        template.add(new ResourcePackage(ArtifactoryUpdatesPanel.class).addJavaScript());

        template.add(new CssClass("news-open"));

        final boolean read = message.getId().equals(CookieUtils.getCookie(READ_COOKIE_NAME));
        if (!read) {
            template.add(new UnreadStyle());
        }
    }

    private static class UnreadStyle extends CssClass {
        private Component component;

        private UnreadStyle() {
            super("news-unread");
        }

        @Override
        public void bind(Component component) {
            this.component = component;
        }

        @Override
        public void renderHead(Component component, IHeaderResponse response) {
            response.renderOnDomReadyJavaScript(
                    String.format("ArtifactoryUpdates.fadeIn('%s');", component.getMarkupId()));
        }
    }
}
