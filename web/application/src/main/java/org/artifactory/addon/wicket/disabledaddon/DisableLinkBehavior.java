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

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.artifactory.common.wicket.behavior.CssClass;

/**
 * @author Yoav Aharoni
 */
public class DisableLinkBehavior extends Behavior {
    @Override
    public void bind(Component component) {
        component.add(new CssClass("disabled"));
    }

    @Override
    public void onConfigure(Component component) {
        AbstractLink link = (AbstractLink) component;
        link.setEnabled(false);
        link.setBeforeDisabledLink("");
        link.setAfterDisabledLink("");
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        tag.setName("a");
    }
}
