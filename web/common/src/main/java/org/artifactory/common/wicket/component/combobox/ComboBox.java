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
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.behavior.combobox.ComboBoxBehavior;

import java.util.List;

/**
 * A Dojo ComboBox widget.<br/> This combo only works with string models and lists.<br/>
 * <b>NOTE!</b> You cannot add ComboBox to ajax target regularly, meaning <b>you
 * can't do <code>target.add(comboBox)</code></b>. Instead add a containing parent:
 * <code>target.add(anyParent)</code> or pass getAjaxTargetMarkupId() as target markup id like so:
 * <code>target.add(comboBox<b>, comboBox.getAjaxTargetMarkupId()</b>)</code>
 *
 * @author Yoav Aharoni
 * @see ComboBox#getAjaxTargetMarkupId()
 */
public class ComboBox extends DropDownChoice<String> {
    public ComboBox(String id) {
        super(id);
    }

    public ComboBox(String id, List<String> choices) {
        super(id, choices);
    }

    public ComboBox(String id, IModel<String> model, List<String> choices) {
        super(id, model, choices);
    }

    public ComboBox(String id, IModel<String> model, IModel<? extends List<? extends String>> choices) {
        super(id, model, choices);
    }

    @Override
    protected String convertChoiceIdToChoice(String id) {
        return id;
    }

    @Override
    protected CharSequence getDefaultChoice(final String selectedValue) {
        return "";
    }

    {
        setChoiceRenderer(new StringChoiceRenderer());
        add(newComboBehavior());
    }

    protected Behavior newComboBehavior() {
        return new ComboBoxBehavior();
    }

    /**
     * When adding ComboBox to ajax target you must also provide the markupId return by this method, like so:
     * <code>target.add(comboBox<b>, comboBox.getAjaxTargetMarkupId()</b>)</code>.
     *
     * @return markupId to be used when calling target.add()
     */
    public String getAjaxTargetMarkupId() {
        return getMarkupId() + "-widget";
    }

    @Override
    public String getModelValue() {
        final Object modelObject = getDefaultModelObject();
        return modelObject == null ? null : modelObject.toString();
    }

    private static class StringChoiceRenderer implements IChoiceRenderer<String> {
        @Override
        public String getDisplayValue(String object) {
            return object;
        }

        @Override
        public String getIdValue(String string, int index) {
            return string;
        }
    }
}
