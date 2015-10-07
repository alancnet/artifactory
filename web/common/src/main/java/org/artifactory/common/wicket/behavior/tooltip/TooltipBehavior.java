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

package org.artifactory.common.wicket.behavior.tooltip;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.behavior.template.TemplateBehavior;

/**
 * @author Yoav Aharoni
 */
public class TooltipBehavior extends TemplateBehavior {
    private IModel messageModel;

    public TooltipBehavior(IModel messageModel) {
        super(TooltipBehavior.class);
        this.messageModel = messageModel;
        getResourcePackage().addJavaScript();
    }

    public boolean isEnabled() {
        return isEnabled(getComponent());
    }

    @Override
    public boolean isEnabled(Component component) {
        return messageModel.getObject() != null;
    }

    @Override
    public void bind(Component component) {
        super.bind(component);
        component.setOutputMarkupId(true);
    }

    public String getMessage() {
        return messageModel.getObject().toString();
    }
}