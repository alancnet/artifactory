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

package org.artifactory.common.wicket.component.panel.titled;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;

/**
 * This panel behaves like a TitledPanel with additional buttons panel at the bottom right, outside of the grey border.
 *
 * @author Yossi Shaul
 */
public abstract class TitledActionPanel extends TitledPanel {
    private RepeatingView buttonsContainer;

    protected TitledActionPanel(String id) {
        super(id);
        init();
    }

    protected TitledActionPanel(String id, IModel iModel) {
        super(id, iModel);
        init();
    }

    private void init() {
        buttonsContainer = new RepeatingView("buttons");
        add(buttonsContainer);
    }

    /**
     * Adds a button to the buttons list and marks the button as the default.
     *
     * @param button The button to add and mark as default.
     */
    protected void addDefaultButton(IFormSubmittingComponent button) {
        addButton((Component) button);
        button.getForm().add(new DefaultButtonBehavior(button));
    }

    /**
     * Adds a button to the buttons list on the bottom left of the panel. The buttons will be displayed in the order
     * they were added.
     *
     * @param button The button to add.
     */
    protected void addButton(Button button) {
        addButton((Component) button);
    }

    /**
     * Adds a button to the buttons list on the bottom left of the panel. The buttons will be displayed in the order
     * they were added.
     *
     * @param button The button to add.
     */
    protected void addButton(AbstractLink button) {
        addButton((Component) button);
    }

    private void addButton(Component button) {
        buttonsContainer.add(button);
    }

    public RepeatingView getButtonsContainer() {
        return buttonsContainer;
    }
}
