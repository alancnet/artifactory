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

package org.artifactory.common.wicket.behavior.template;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.request.cycle.RequestCycle;
import org.artifactory.common.wicket.behavior.template.loadingstrategy.BaseTemplateStrategy;
import org.artifactory.common.wicket.behavior.template.loadingstrategy.CachedInterpolatedTemplateStrategy;
import org.artifactory.common.wicket.contributor.ResourcePackage;

/**
 * @author Yoav Aharoni
 */
public class TemplateBehavior extends Behavior {
    private transient ResourcePackage resourcePackage;
    private BaseTemplateStrategy templateStrategy;

    private Class<? extends TemplateBehavior> resourceClass;

    private Component component;

    public TemplateBehavior(Class<? extends TemplateBehavior> resourceClass) {
        init(resourceClass);
    }

    private void init(Class<? extends TemplateBehavior> resourceClass) {
        this.resourceClass = resourceClass;
        resourcePackage = newResourcePackage();
        templateStrategy = new CachedInterpolatedTemplateStrategy();
    }

    public Class<? extends TemplateBehavior> getResourceClass() {
        return resourceClass;
    }

    @Override
    public void bind(Component component) {
        super.bind(component);
        this.component = component;
        component.add(getResourcePackage());
    }

    @Override
    public void beforeRender(Component component) {
        super.beforeRender(component);
        templateStrategy.load(this);
        write(templateStrategy.getBeforeRenderString());
    }

    @Override
    public void afterRender(Component component) {
        super.afterRender(component);
        write(templateStrategy.getAfterRenderString());
    }

    protected ResourcePackage newResourcePackage() {
        return new ResourcePackage(getResourceClass());
    }

    public ResourcePackage getResourcePackage() {
        return resourcePackage;
    }

    public Component getComponent() {
        return component;
    }

    public BaseTemplateStrategy getTemplateStrategy() {
        return templateStrategy;
    }

    public void setTemplateStrategy(BaseTemplateStrategy templateStrategy) {
        this.templateStrategy = templateStrategy;
    }

    protected void assertTagName(ComponentTag tag, String tagName) {
        if (!tagName.equalsIgnoreCase(tag.getName())) {
            throw new RuntimeException(getClass() + " can only be added to "
                    + tagName + " tag but was added to " + tag.getName() + " tag.");
        }
    }

    private static void write(String render) {
        RequestCycle.get().getResponse().write(render);
    }

    public static void addAttribute(ComponentTag tag, String attName, String addValue, String sep) {
        String value = tag.getAttributes().getString(attName);
        if (StringUtils.isEmpty(value)) {
            tag.put(attName, addValue);
        } else {
            tag.put(attName, value + sep + addValue);
        }
    }

    public static void addCssClass(ComponentTag tag, String cssClass) {
        addAttribute(tag, "class", cssClass, " ");
    }
}
