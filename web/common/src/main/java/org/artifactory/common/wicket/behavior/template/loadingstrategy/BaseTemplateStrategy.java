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

package org.artifactory.common.wicket.behavior.template.loadingstrategy;

import org.artifactory.common.wicket.behavior.template.TemplateBehavior;
import org.artifactory.common.wicket.util.WicketUtils;

import java.io.Serializable;

/**
 * @author Yoav Aharoni
 */
public abstract class BaseTemplateStrategy implements Serializable {
    private String beforeRenderString;
    private String afterRenderString;

    public abstract void load(TemplateBehavior behavior);

    public String getBeforeRenderString() {
        return beforeRenderString;
    }

    public String getAfterRenderString() {
        return afterRenderString;
    }

    protected String getDefaultTemplate(TemplateBehavior behavior) {
        final Class<? extends TemplateBehavior> resourceClass = behavior.getResourceClass();
        String filename = resourceClass.getSimpleName() + ".html";
        return WicketUtils.readResource(resourceClass, filename);
    }

    public void setTemplate(String template) {
        String[] strings = template.split("<wicket:body/>", 2);
        if (strings.length != 2) {
            throw new IllegalArgumentException("Template must contain one <wicket:body/> tag.");
        }
        beforeRenderString = strings[0];
        afterRenderString = strings[1];
    }

    public boolean isLoaded() {
        return beforeRenderString != null;
    }
}
