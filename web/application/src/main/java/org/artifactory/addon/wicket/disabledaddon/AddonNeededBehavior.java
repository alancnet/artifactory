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

package org.artifactory.addon.wicket.disabledaddon;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.artifactory.addon.AddonType;
import org.artifactory.common.wicket.behavior.template.TemplateBehavior;
import org.artifactory.common.wicket.behavior.tooltip.TooltipBehavior;
import org.artifactory.common.wicket.contributor.ResourcePackage;
import org.artifactory.common.wicket.util.JavaScriptUtils;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.descriptor.addon.AddonSettings;
import org.artifactory.webapp.servlet.RequestUtils;
import org.artifactory.webapp.wicket.application.ArtifactoryApplication;

import javax.servlet.http.Cookie;

/**
 * @author Yoav Aharoni
 */
public class AddonNeededBehavior extends TemplateBehavior {
    private static final String MESSAGE_KEY = "addon.disabled";

    private AddonType addon;
    private boolean enabled;
    private String position;
    private String targetId; // target container for the icon and bubble
    private String iconClassName = "addon-icon";

    public AddonNeededBehavior(AddonType addon) {
        super(AddonNeededBehavior.class);
        this.addon = addon;
        this.enabled = getAddonSettings().isShowAddonsInfo()
                && !getCookie("addon-" + addon.getAddonName()).equals(getServerToken());

        ResourcePackage resourcePackage = getResourcePackage();
        resourcePackage.dependsOn(new ResourcePackage(TooltipBehavior.class).addJavaScript());
        resourcePackage.addJavaScript();
    }


    @Override
    public void beforeRender(Component component) {
        if (enabled) {
            super.beforeRender(component);
        }
    }

    @Override
    public void afterRender(Component component) {
        if (enabled) {
            super.afterRender(component);
        }
    }

    public String getPosition() {
        return StringUtils.isEmpty(position) ? "null" : position;
    }

    public AddonNeededBehavior setPosition(String... position) {
        this.position = JavaScriptUtils.jsParam(position);
        return this;
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);

        if (!enabled) {
            tag.put("style", "display: none;");
        } else {
            addCssClass(tag, "disabled-addon");
        }
    }

    @Override
    public void bind(Component component) {
        super.bind(component);
        component.setOutputMarkupId(true);
        if (targetId == null) {
            targetId = component.getMarkupId();
        }
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);
        final String contextPrefix = RequestUtils.getContextPrefix(WicketUtils.getHttpServletRequest());
        response.renderJavaScript(String.format("var artApp = '/%s'", contextPrefix), null);
    }

    public AddonType getAddon() {
        return addon;
    }

    public String getServerToken() {
        return getAddonSettings().getShowAddonsInfoCookie();
    }

    private AddonSettings getAddonSettings() {
        return ArtifactoryApplication.get().getCentralConfig().getDescriptor().getAddons();
    }

    private String getCookie(String name) {
        Request request = RequestCycle.get().getRequest();
        if (request instanceof WebRequest) {
            Cookie cookie = ((WebRequest) request).getCookie(name);
            if (cookie == null) {
                return "";
            }
            return cookie.getValue();
        }
        return "";
    }

    public String getMessage() {
        return getComponent().getString(MESSAGE_KEY, Model.of(addon));
    }

    public String getTargetId() {
        return targetId;
    }

    public AddonNeededBehavior setTargetId(String targetId) {
        this.targetId = targetId;
        return this;
    }

    public String getIconClassName() {
        return iconClassName;
    }

    public AddonNeededBehavior setIconClassName(String iconClassName) {
        this.iconClassName = iconClassName;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }
}