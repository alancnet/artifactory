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

package org.artifactory.common.wicket.behavior.defaultbutton;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.artifactory.common.wicket.behavior.CssClass;

/**
 * @author Yoav Aharoni
 */
public class DefaultButtonBehavior extends Behavior {
    private IFormSubmittingComponent defaultButton;

    public DefaultButtonBehavior(IFormSubmittingComponent defaultButton) {
        this.defaultButton = defaultButton;
        ((Component) defaultButton).setOutputMarkupId(true);
    }

    @Override
    public void bind(Component component) {
        super.bind(component);
        if (!(component instanceof Form)) {
            throw new IllegalArgumentException(DefaultButtonBehavior.class.getSimpleName()
                    + " can only be added to Form components.");
        }

        Form form = (Form) component;
        form.setDefaultButton(defaultButton);
        final Component button = (Component) defaultButton;
        button.add(new CssClass(new DefaultButtonStyleModel(button)));
    }
}
