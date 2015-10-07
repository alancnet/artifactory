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
import org.apache.wicket.util.value.IValueMap;
import org.artifactory.addon.AddonType;
import org.artifactory.common.wicket.behavior.border.TitledBorderBehavior;

/**
 * @author Yoav Aharoni
 */
public class DisabledAddonBehavior extends AddonNeededBehavior {
    public DisabledAddonBehavior(AddonType addon) {
        super(addon);
    }

    @Override
    public void bind(Component component) {
        if (getTargetId() == null) {
            // If we have a titled border behavior, than place the disabled-addon icon within the border title
            for (Behavior b : component.getBehaviors()) {
                if (b instanceof TitledBorderBehavior) {
                    if (!isEnabled()) {
                        component.remove(b);
                    }
                    setTargetId(component.getMarkupId() + "-border-icon");
                    ((TitledBorderBehavior) b).setCssClass("disabled-addon");
                }
            }
        }
        super.bind(component);
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);
        IValueMap attributes = tag.getAttributes();
        attributes.remove("href");
        attributes.remove("onclick");
    }
}
