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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.behavior.combobox.history.HistoryComboBoxBehavior;

import java.util.List;

import static org.artifactory.common.wicket.util.JavaScriptUtils.jsParam;

/**
 * @author Yoav Aharoni
 */
public class HistoryComboBox extends ComboBox {
    public HistoryComboBox(String id, List<String> choices) {
        super(id, choices);
    }

    public HistoryComboBox(String id, IModel<String> model, IModel<List<String>> choices) {
        super(id, model, choices);
    }

    public void addHistory() {
        addHistory(getDefaultModelObjectAsString());
    }

    public void addHistory(String value) {
        AjaxRequestTarget.get().prependJavaScript(
                String.format("dijit.byId(%s).addHistory(%s)", jsParam(getMarkupId()), jsParam(value)));
    }

    public void clearHistory() {
        AjaxRequestTarget.get()
                .prependJavaScript(String.format("dijit.byId(%s).clearHistory()", jsParam(getMarkupId())));
    }

    @Override
    protected Behavior newComboBehavior() {
        return new HistoryComboBoxBehavior();
    }
}
