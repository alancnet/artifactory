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

package org.artifactory.common.wicket.component.table.columns;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.table.columns.panel.textfield.TextFieldPanel;

/**
 * A {@link AbstractColumn} that displays a text field in column. For editable text fields one should override
 * {@link TextFieldColumn#newTextField(java.lang.String, org.apache.wicket.model.IModel<T>, T)}.
 *
 * @author Yoav Aharoni
 */
public class TextFieldColumn<T> extends AbstractColumn<T> {
    private String expression;

    /**
     * Construct new text field column with the given property expression for property access and sorting.
     *
     * @param title      The columns title
     * @param expression Property expression which will be used to extract value from the raw object, both for
     *                   display and as sorting property
     */
    public TextFieldColumn(String title, String expression) {
        super(Model.of(title), expression);
        this.expression = expression;
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> rowModel) {
        MarkupContainer panel = new TextFieldPanel(componentId, rowModel);
        cellItem.add(new CssClass("TextFieldColumn"));
        cellItem.add(panel);

        T rowObject = rowModel.getObject();
        PropertyModel<String> model = newPropertyModel(rowObject);
        FormComponent textField = newTextField(TextFieldPanel.TEXTFIELD_ID, model, rowObject);
        panel.add(textField);
    }

    /**
     * @param id         Wicket component id
     * @param valueModel Model for the value to display in the panel (string)
     * @param rowObject  The row object (which the value model gets the information from)
     * @return Construct a new text field.
     */
    protected TextField<String> newTextField(String id, IModel<String> valueModel, T rowObject) {
        return new TextField<>(id, valueModel);
    }

    protected PropertyModel<String> newPropertyModel(T rowObject) {
        return new PropertyModel<>(rowObject, expression);
    }

    public final String getExpression() {
        return expression;
    }
}