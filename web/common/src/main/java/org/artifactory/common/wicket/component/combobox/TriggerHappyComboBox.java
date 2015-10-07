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

package org.artifactory.common.wicket.component.combobox;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.behavior.combobox.TriggerHappyComboBoxBehavior;

import java.util.List;

/**
 * ComboBox which triggers onChange on every item selection, if value didn't seem to change.
 *
 * @author Yoav Aharoni
 */
public class TriggerHappyComboBox extends ComboBox {

    public TriggerHappyComboBox(String id) {
        super(id);
    }

    public TriggerHappyComboBox(String id, List<String> choices) {
        super(id, choices);
    }

    public TriggerHappyComboBox(String id, IModel<String> model, List<String> choices) {
        super(id, model, choices);
    }

    public TriggerHappyComboBox(String id, IModel<String> model, IModel<? extends List<? extends String>> choices) {
        super(id, model, choices);
    }

    @Override
    protected Behavior newComboBehavior() {
        return new TriggerHappyComboBoxBehavior();
    }
}
